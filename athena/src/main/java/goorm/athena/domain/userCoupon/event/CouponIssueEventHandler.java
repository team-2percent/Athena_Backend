package goorm.athena.domain.userCoupon.event;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.coupon.service.CouponQueryService;
import goorm.athena.domain.notification.service.FcmNotificationService;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.service.UserQueryService;
import goorm.athena.domain.userCoupon.dto.req.UserCouponIssueRequest;
import goorm.athena.domain.userCoupon.entity.UserCoupon;
import goorm.athena.domain.userCoupon.repository.UserCouponRepository;
import goorm.athena.domain.userCoupon.service.*;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponIssueEventHandler {

    private final FcmNotificationService fcmNotificationService;
    private final UserQueryService userQueryService;
    private final CouponQueryService couponQueryService;
    private final UserCouponRepository userCouponRepository;

    @Async
    @EventListener
    @Transactional
    public void handleCouponIssueEvent(CouponIssueEvent event) {
        Long userId = event.userId();
        Long couponId = event.couponId();

        // 1. User, Coupon 조회 (필요하면 캐싱 전략 고민 가능)
        User user = userQueryService.getUser(userId);
        Coupon coupon = couponQueryService.getCoupon(couponId);

        // 2. UserCoupon 엔티티 생성 및 저장
        UserCoupon userCoupon = UserCoupon.create(user, coupon);
        userCouponRepository.save(userCoupon);

        fcmNotificationService.notifyCoupon(event.couponTitle());
    }
}
