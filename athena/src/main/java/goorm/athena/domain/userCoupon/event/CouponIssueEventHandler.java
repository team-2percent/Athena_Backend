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
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponIssueEventHandler {

    private final FcmNotificationService fcmNotificationService;

    @Async
    @EventListener
    public void handleCouponIssueEvent(CouponIssueEvent event) {
        fcmNotificationService.notifyCoupon(event.couponTitle());
    }
}
