package goorm.athena.domain.userCoupon.service.test;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.service.CouponQueryService;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.service.UserQueryService;
import goorm.athena.domain.userCoupon.dto.req.UserCouponIssueRequest;
import goorm.athena.domain.userCoupon.dto.res.UserCouponIssueResponse;
import goorm.athena.domain.userCoupon.entity.UserCoupon;
import goorm.athena.domain.userCoupon.mapper.UserCouponMapper;
import goorm.athena.domain.userCoupon.repository.UserCouponRepository;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

// RLock 을 통해 Redisson 분산락 적용
// 하나의 쿠폰 발급에 대한 동시성 제어 가능 및 중복 검증, 재고 감소가 한 트랜잭션에서 이뤄짐
// 락 획득 시 대기 시간이 존재하여 성능에 저하 가능성이 있음

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCouponCommandServiceV4_3 {
    private final UserQueryService userQueryService;
    private final CouponQueryService couponQueryService;
    private final UserCouponRepository userCouponRepository;
    private final UserCouponMapper userCouponMapper;
    private final RedissonClient redissonClient;  // Redis 클라이언트 주입

    @Transactional
    public UserCouponIssueResponse issueCoupon(Long userId, UserCouponIssueRequest request) {
        String totalKey = "coupon_total_" + request.couponId();
        String usedKey = "coupon_used_" + request.couponId();
        String lockKey = "coupon_lock_" + request.couponId();

        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(5, 3, TimeUnit.SECONDS);
            if(!isLocked){
                throw new CustomException(ErrorCode.COUPON_OUT_STOCK);
            }

            // 1. Redis에서 총 재고와 사용량을 읽어온다
            RAtomicLong totalAtomic = redissonClient.getAtomicLong(totalKey);
            RAtomicLong usedAtomic = redissonClient.getAtomicLong(usedKey);

            Long total = totalAtomic.get();
            Long used = usedAtomic.get();

            if (total == null) {
                throw new CustomException(ErrorCode.COUPON_NOT_FOUND);
            }

            if (used == null) {
                used = 0L;
            }

            // 2. 재고 확인
            if (used >= total) {
                throw new CustomException(ErrorCode.COUPON_OUT_STOCK);
            }

            // 3. 사용량 증가 (원자적 연산)
            long currentUsed = usedAtomic.incrementAndGet();

            // 증가 후 체크: 동시에 여러 요청이 왔을 수 있으므로 마지노선 체크
            if (currentUsed > total) {
                // 초과로 증가했을 경우 롤백
                usedAtomic.decrementAndGet();
                throw new CustomException(ErrorCode.COUPON_OUT_STOCK);
            }

            // 4. DB 처리
            User user = userQueryService.getUser(userId);
            Coupon coupon = couponQueryService.getCoupon(request.couponId());

            if (userCouponRepository.existsByUserAndCoupon(user, coupon)) {
                usedAtomic.decrementAndGet();
                throw new CustomException(ErrorCode.ALREADY_ISSUED_COUPON);
            }

            UserCoupon userCoupon = UserCoupon.create(user, coupon);
            userCouponRepository.save(userCoupon);

            return userCouponMapper.toCreateResponse(userCoupon);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.INVALID_COUPON_STATUS);
        } finally {
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }
}