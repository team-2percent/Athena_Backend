package goorm.athena.domain.userCoupon.service.test;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.locks.ReentrantLock;

// 애플리케이션 단위에서 락 적용 ( ReentrantLock 적용 ), 정합성 이슈
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCouponCommandServiceV1 {
    private final UserQueryService userQueryService;
    private final CouponQueryService couponQueryService;
    private final UserCouponRepository userCouponRepository;
    private final UserCouponMapper userCouponMapper;

    private final ReentrantLock lock = new ReentrantLock();

    @Transactional
    public UserCouponIssueResponse issueCoupon(Long userId, UserCouponIssueRequest request) {
        lock.lock();
        try {
            User user = userQueryService.getUser(userId);
            Coupon coupon = couponQueryService.getCoupon(request.couponId());

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
        } finally {
            lock.unlock();
        }
    }
}