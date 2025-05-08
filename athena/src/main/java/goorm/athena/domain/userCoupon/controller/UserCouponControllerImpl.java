package goorm.athena.domain.userCoupon.controller;

import goorm.athena.domain.userCoupon.dto.req.UserCouponCreateRequest;
import goorm.athena.domain.userCoupon.dto.res.UserCouponCreateResponse;
import goorm.athena.domain.userCoupon.service.UserCouponService;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/userCoupon")
public class UserCouponControllerImpl implements UserCouponController {
    private final UserCouponService userCouponService;

    @Override
    @PostMapping
    public ResponseEntity<UserCouponCreateResponse> issueCoupon(@CheckLogin LoginUserRequest loginUserRequest,
                                                                @RequestBody UserCouponCreateRequest request){
        UserCouponCreateResponse response = userCouponService.issueCoupon(loginUserRequest.userId(), request);
        return ResponseEntity.ok(response);
    }
}
