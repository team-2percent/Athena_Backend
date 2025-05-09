package goorm.athena.domain.userCoupon.controller;

import goorm.athena.domain.userCoupon.dto.req.UserCouponIssueRequest;
import goorm.athena.domain.userCoupon.dto.res.UserCouponGetResponse;
import goorm.athena.domain.userCoupon.dto.res.UserCouponIssueResponse;
import goorm.athena.domain.userCoupon.scheduler.UserCouponScheduler;
import goorm.athena.domain.userCoupon.service.UserCouponService;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
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

    @Override
    @PostMapping
    public ResponseEntity<UserCouponIssueResponse> issueCoupon(@CheckLogin LoginUserRequest loginUserRequest,
                                                               @RequestBody UserCouponIssueRequest request){
        UserCouponIssueResponse response = userCouponService.issueCoupon(loginUserRequest.userId(), request);
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/use")
    public ResponseEntity<Void> useCoupon(@CheckLogin LoginUserRequest loginUserRequest,
                                          @RequestBody Long userCouponId){
        userCouponService.useCoupon(loginUserRequest.userId(), userCouponId);
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/scheduler")
    public void schedulerExpiredUserCoupon(){
        userCouponScheduler.expiredUserCoupon();
    }

    @Override
    @GetMapping
    public ResponseEntity<List<UserCouponGetResponse>> getUserCoupon(@CheckLogin LoginUserRequest loginUserRequest){
        List<UserCouponGetResponse> response = userCouponService.getUserCoupon(loginUserRequest.userId());
        return ResponseEntity.ok(response);
    }
}
