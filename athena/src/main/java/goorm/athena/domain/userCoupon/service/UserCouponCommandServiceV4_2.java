package goorm.athena.domain.userCoupon.service;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.repository.CouponRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCouponCommandServiceV4_2 {
    private final UserQueryService userQueryService;
    private final CouponQueryService couponQueryService;
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserCouponMapper userCouponMapper;
    private final RedissonClient redissonClient;  // Redis 클라이언트 주입

    @Transactional
    public void issueCoupon(Long userId, UserCouponIssueRequest request) {
        Long couponId = request.couponId();
        String totalKey = "coupon_total_" + couponId;
        String usedKey = "coupon_used_" + couponId;
        String successKey = "coupon_success_" + couponId;

        // 1. 총량 확인
        RAtomicLong totalAtomic = redissonClient.getAtomicLong(totalKey);
        RAtomicLong usedAtomic = redissonClient.getAtomicLong(usedKey);
        RAtomicLong successAtomic = redissonClient.getAtomicLong(successKey);

        long total = totalAtomic.get();
        long used = usedAtomic.incrementAndGet(); // ✅ 먼저 선점

        if (used > total) {
            usedAtomic.decrementAndGet(); // ❌ 선점 취소
            throw new CustomException(ErrorCode.COUPON_OUT_STOCK);
        }

        // 2. 사용자/쿠폰 중복 확인 (조심)
        User user = userQueryService.getUser(userId);
        Coupon coupon = couponQueryService.getCoupon(couponId);

        if (userCouponRepository.existsByUserAndCoupon(user, coupon)) {
            usedAtomic.decrementAndGet(); // 중복일 경우 롤백
            throw new CustomException(ErrorCode.ALREADY_ISSUED_COUPON);
        }

        // 3. DB 저장
        UserCoupon userCoupon = UserCoupon.create(user, coupon);
        userCouponRepository.save(userCoupon);

        // 4. 성공 수 증가
        successAtomic.incrementAndGet();

    }
}