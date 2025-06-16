package goorm.athena.domain.userCoupon.event;

import goorm.athena.domain.notification.service.FcmNotificationService;
import goorm.athena.domain.userCoupon.dto.req.UserCouponIssueRequest;
import goorm.athena.domain.userCoupon.service.UserCouponCommandServiceV4_2;
import goorm.athena.domain.userCoupon.service.UserCouponCommandServiceV4_3;
import goorm.athena.domain.userCoupon.service.UserCouponCommandServiceV4_4;
import goorm.athena.domain.userCoupon.service.UserCouponQueryService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponIssueEventHandler {

    private final UserCouponCommandServiceV4_4 userCouponCommandService;
    private final UserCouponQueryService userCouponQueryService;
    private final FcmNotificationService fcmNotificationService;

    @Async
    @EventListener
    public void handleCouponIssueEvent(CouponIssueEvent event) {
        String couponTitle = userCouponQueryService.getCouponTitle(event.couponId());
        fcmNotificationService.notifyCoupon(couponTitle);
    }
}
