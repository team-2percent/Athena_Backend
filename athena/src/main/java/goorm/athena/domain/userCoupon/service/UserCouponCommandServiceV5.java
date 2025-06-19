package goorm.athena.domain.userCoupon.service;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.repository.CouponRepository;
import goorm.athena.domain.coupon.service.CouponQueryService;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.service.UserQueryService;
import goorm.athena.domain.userCoupon.dto.req.UserCouponIssueRequest;
import goorm.athena.domain.userCoupon.dto.res.UserCouponIssueResponse;
import goorm.athena.domain.userCoupon.entity.UserCoupon;
import goorm.athena.domain.userCoupon.event.CouponIssueEvent;
import goorm.athena.domain.userCoupon.mapper.UserCouponMapper;
import goorm.athena.domain.userCoupon.repository.UserCouponRepository;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/*
    큐로 비동기 발급 후 DB에 반영하며 락이 없기 때문에 속도가 매우 빠름
    이벤트 기반으로 확장이 쉬우나 정확한 정합성이 필요하며 난이도가 매우 어려움
    대신 완전한 이벤트 기반 아키텍처로 확장 가능성이 큼
 */
@Service
@RequiredArgsConstructor
public class UserCouponCommandServiceV5 {

    private final RedissonClient redissonClient;
    private final UserQueryService userQueryService;
    private final CouponQueryService couponQueryService;
    private final UserCouponRepository userCouponRepository;
    private final CouponRepository couponRepository;
    private final UserCouponMapper userCouponMapper;
    private final ApplicationEventPublisher eventPublisher;

    public UserCouponIssueResponse issueCoupon(Long userId, UserCouponIssueRequest request) {
        Long couponId = request.couponId();

        checkAndMarkIssuedWithLock(couponId, userId);
        UserCouponIssueResponse response = saveCouponIssue(userId, couponId);
        eventPublisher.publishEvent(new CouponIssueEvent(userId, couponId));

        return response;
    }

    // Redis에서 중복 및 재고 체크 + 락 적용 예시
    public void checkAndMarkIssuedWithLock(Long couponId, Long userId) {
        String lockKey = "lock_coupon_issue_" + couponId;
        var lock = redissonClient.getLock(lockKey);

        try {
            // 락 획득 (최대 5초 대기, 10초 자동해제)
            if (lock.tryLock(5, 10, java.util.concurrent.TimeUnit.SECONDS)) {
                try {
                    String totalKey = "coupon_total_" + couponId;
                    String issuedSetKey = "issued_users_" + couponId;

                    RSet<String> issuedSet = redissonClient.getSet(issuedSetKey, StringCodec.INSTANCE);
                    RBucket<String> totalBucket = redissonClient.getBucket(totalKey, StringCodec.INSTANCE);

                    // 중복 체크
                    boolean added = issuedSet.add(String.valueOf(userId));
                    if (!added) {
                        throw new CustomException(ErrorCode.ALREADY_ISSUED_COUPON);
                    }

                    // 재고 확인
                    Long total = Long.valueOf(totalBucket.get());
                    long issuedCount = issuedSet.size();

                    if (issuedCount > total) {
                        // 재고 부족시 롤백
                        issuedSet.remove(String.valueOf(userId));
                        throw new CustomException(ErrorCode.COUPON_OUT_STOCK);
                    }
/*
                    if (issuedCount == total) {
                        Coupon coupon = couponQueryService.getCoupon(couponId);
                        if (coupon.getStock() > 0) {
                            coupon.markAsSoldOut();
                            couponRepository.save(coupon);
                            System.out.println(couponRepository.findById(coupon.getId()).get().getStock());
                        }
                    }

 */

                } finally {
                    lock.unlock();
                }
            } else {
                throw new CustomException(ErrorCode.INVALID_COUPON_STATUS); // 락 획득 실패 시 예외
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.ALREADY_ISSUED_COUPON);
        }
    }

    // Redis 롤백 메서드 분리
    private void rollbackRedis(Long couponId, Long userId) {
        String issuedSetKey = "issued_users_" + couponId;
        redissonClient.getSet(issuedSetKey).remove(String.valueOf(userId));
    }

    @Transactional
    public UserCouponIssueResponse saveCouponIssue(Long userId, Long couponId) {
        User user = userQueryService.getUser(userId);
        Coupon coupon = couponQueryService.getCoupon(couponId);

        try {
            UserCoupon userCoupon = UserCoupon.create(user, coupon);
            userCouponRepository.save(userCoupon);
            return userCouponMapper.toCreateResponse(userCoupon);
        } catch (DataIntegrityViolationException e) {
            rollbackRedis(couponId, userId);
            throw new CustomException(ErrorCode.ALREADY_ISSUED_COUPON);
        } catch (RuntimeException e) {
            rollbackRedis(couponId, userId);
            throw e;
        }
    }
}
