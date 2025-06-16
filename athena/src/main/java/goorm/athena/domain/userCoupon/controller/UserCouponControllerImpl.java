package goorm.athena.domain.userCoupon.controller;

import goorm.athena.domain.notification.service.FcmNotificationService;
import goorm.athena.domain.userCoupon.dto.req.UserCouponIssueRequest;
import goorm.athena.domain.userCoupon.dto.req.UserCouponUseRequest;
import goorm.athena.domain.userCoupon.dto.res.UserCouponIssueResponse;
import goorm.athena.domain.userCoupon.scheduler.UserCouponScheduler;
import goorm.athena.domain.userCoupon.service.*;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/userCoupon")
public class UserCouponControllerImpl implements UserCouponController {
    private final UserCouponCommandService userCouponCommandService;
    private final UserCouponQueryService userCouponQueryService;
    private final UserCouponCommandServiceV1 userCouponCommandServiceV1;
    private final UserCouponCommandServiceV2 userCouponCommandServiceV2;
    private final UserCouponCommandServiceV3 userCouponCommandServiceV3;
    private final UserCouponCommandServiceV4_4 userCouponCommandServiceV4;
    private final UserCouponCommandServiceV4_5 userCouponCommandServiceV5;
    private final UserCouponCommandServiceV4_6 userCouponCommandServiceV6;
    private final UserCouponScheduler userCouponScheduler;
    private final FcmNotificationService fcmNotificationService;

    @Override
    @PostMapping
    public ResponseEntity<UserCouponIssueResponse> issueCoupon(@CheckLogin LoginUserRequest loginUserRequest,
                                                               @RequestBody UserCouponIssueRequest request){
        userCouponCommandServiceV6.issueCoupon(loginUserRequest.userId(), request);

        String couponTitle = userCouponQueryService.getCouponTitle(request.couponId());
        fcmNotificationService.notifyCoupon(couponTitle);

        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/use")
    public ResponseEntity<Void> useCoupon(@CheckLogin LoginUserRequest loginUserRequest,
                                          @RequestBody UserCouponUseRequest request){
        userCouponCommandService.useCoupon(loginUserRequest.userId(), request.userCouponId());
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/scheduler")
    public void schedulerExpiredUserCoupon(){
        userCouponScheduler.expiredUserCoupon();
    }

}
