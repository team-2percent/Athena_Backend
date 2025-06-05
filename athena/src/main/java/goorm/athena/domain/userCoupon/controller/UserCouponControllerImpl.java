package goorm.athena.domain.userCoupon.controller;

import goorm.athena.domain.notification.service.FcmNotificationService;
import goorm.athena.domain.userCoupon.dto.cursor.UserCouponCursorResponse;
import goorm.athena.domain.userCoupon.dto.req.UserCouponIssueRequest;
import goorm.athena.domain.userCoupon.dto.req.UserCouponUseRequest;
import goorm.athena.domain.userCoupon.dto.res.UserCouponGetResponse;
import goorm.athena.domain.userCoupon.dto.res.UserCouponIssueResponse;
import goorm.athena.domain.userCoupon.scheduler.UserCouponScheduler;
import goorm.athena.domain.userCoupon.service.UserCouponService;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/userCoupon")
public class UserCouponControllerImpl implements UserCouponController {
    private final UserCouponService userCouponService;
    private final UserCouponScheduler userCouponScheduler;
    private final FcmNotificationService fcmNotificationService;

    @Override
    @PostMapping
    public ResponseEntity<UserCouponIssueResponse> issueCoupon(@CheckLogin LoginUserRequest loginUserRequest,
                                                               @RequestBody UserCouponIssueRequest request){
        UserCouponIssueResponse response = userCouponService.issueCoupon(loginUserRequest.userId(), request);

        String couponTitle = userCouponService.getCouponTitle(request.couponId());
        fcmNotificationService.notifyCoupon(couponTitle);

        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/use")
    public ResponseEntity<Void> useCoupon(@CheckLogin LoginUserRequest loginUserRequest,
                                          @RequestBody UserCouponUseRequest request){
        userCouponService.useCoupon(loginUserRequest.userId(), request.userCouponId());
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/scheduler")
    public void schedulerExpiredUserCoupon(){
        userCouponScheduler.expiredUserCoupon();
    }

}
