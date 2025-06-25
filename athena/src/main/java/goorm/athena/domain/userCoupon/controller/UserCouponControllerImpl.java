package goorm.athena.domain.userCoupon.controller;

import goorm.athena.domain.notification.service.NotificationService;
import goorm.athena.domain.userCoupon.dto.req.UserCouponIssueRequest;
import goorm.athena.domain.userCoupon.dto.req.UserCouponUseRequest;
import goorm.athena.domain.userCoupon.dto.res.UserCouponIssueResponse;
import goorm.athena.domain.userCoupon.scheduler.UserCouponScheduler;
import goorm.athena.domain.userCoupon.service.UserCouponCommandService;
import goorm.athena.domain.userCoupon.service.UserCouponQueryService;
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
    private final UserCouponScheduler userCouponScheduler;
    private final NotificationService notificationService;

    @Override
    @PostMapping
    public ResponseEntity<UserCouponIssueResponse> issueCoupon(@CheckLogin LoginUserRequest loginUserRequest,
                                                               @RequestBody UserCouponIssueRequest request){
        UserCouponIssueResponse response = userCouponCommandService.issueCoupon(loginUserRequest.userId(), request);

        String couponTitle = userCouponQueryService.getCouponTitle(request.couponId());
        notificationService.notifyCoupon(couponTitle);

        return ResponseEntity.ok(response);
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
