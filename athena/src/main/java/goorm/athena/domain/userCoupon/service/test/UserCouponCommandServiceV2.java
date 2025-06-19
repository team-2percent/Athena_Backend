package goorm.athena.domain.userCoupon.service.test;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
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
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// DB단계에서 락 적용 ( 비관적 락 ), 정합성은 해결됐으나 DB에 부하가 심함
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCouponCommandServiceV2 {
    private final UserQueryService userQueryService;
    private final CouponQueryService couponQueryService;
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserCouponMapper userCouponMapper;

    private final RedissonClient redissonClient;

    @Transactional
    public UserCouponIssueResponse issueCoupon(Long userId, UserCouponIssueRequest request) {
        // 락 키를 couponId 기반으로 생성 (쿠폰 단위로 락을 걸음)
        String lockKey = "coupon_issue_lock_" + request.couponId();
        RLock lock = redissonClient.getLock(lockKey);

        User user = userQueryService.getUser(userId);
        Coupon coupon = couponRepository.findByIdForUpdate(request.couponId()).orElseThrow(
                () -> new CustomException(ErrorCode.COUPON_NOT_FOUND)
        );

        // 1. 발급받을 수 있는 쿠폰인지 확인
        if (!coupon.getCouponStatus().equals(CouponStatus.IN_PROGRESS)) {
            throw new CustomException(ErrorCode.INVALID_COUPON_STATUS);
        }

        // 2. 이미 발급받은 쿠폰인지 확인
        if (userCouponRepository.existsByUserAndCoupon(user, coupon)) {
            throw new CustomException(ErrorCode.ALREADY_ISSUED_COUPON);
        }

        coupon.decreaseStock();

        UserCoupon userCoupon = UserCoupon.create(user, coupon);
        userCouponRepository.save(userCoupon);

        return userCouponMapper.toCreateResponse(userCoupon);
    }
}