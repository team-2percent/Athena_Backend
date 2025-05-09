package goorm.athena.domain.userCoupon.controller;

import goorm.athena.domain.userCoupon.dto.req.UserCouponIssueRequest;
import goorm.athena.domain.userCoupon.dto.res.UserCouponIssueResponse;
import goorm.athena.domain.userCoupon.entity.UserCoupon;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "UserCoupon", description = "유저 쿠폰 관련 API")
@RequestMapping("/api/userCoupon")
public interface UserCouponController {

    @Operation(summary = "유저 쿠폰 생성 API", description = "해당 쿠폰을 유저에게 발급합니다.")
    @ApiResponse(responseCode = "200", description = "유저의 새 쿠폰이 생성되었습니다.",
        content = @Content(schema = @Schema(implementation = UserCoupon.class)))
    @PostMapping
    public ResponseEntity<UserCouponIssueResponse> issueCoupon(@Parameter(hidden = true) @CheckLogin LoginUserRequest loginUserRequest,
                                                               @RequestBody UserCouponIssueRequest request);

    @Operation(summary = "유저의 쿠폰 사용 API", description = "유저가 해당 쿠폰의 ID를 받아 사용합니다." +
            "쿠폰 사용 예시 로직이며, 쿠폰만을 사용하는 컨트롤러 없이 다른 서비스에 사용될 수 있습니다.")
    @ApiResponse(responseCode = "203", description = "해당 유저가 보유한 쿠폰을 사용했습니다.")
    @PostMapping
    public ResponseEntity<Void> useCoupon(@Parameter(hidden = true) @CheckLogin LoginUserRequest loginUserRequest,
                                          @RequestBody Long userCouponId);

    @PostMapping("/scheduler")
    public void schedulerExpiredUserCoupon();

}
