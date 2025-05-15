package goorm.athena.domain.userCoupon.controller;

import goorm.athena.domain.couponEvent.dto.res.CouponEventGetResponse;
import goorm.athena.domain.userCoupon.dto.cursor.UserCouponCursorResponse;
import goorm.athena.domain.userCoupon.dto.req.UserCouponIssueRequest;
import goorm.athena.domain.userCoupon.dto.req.UserCouponUseRequest;
import goorm.athena.domain.userCoupon.dto.res.UserCouponGetResponse;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "UserCoupon", description = "유저 쿠폰 관련 API")
@RequestMapping("/api/userCoupon")
public interface UserCouponController {

    @Operation(summary = "유저 쿠폰 생성 API", description = "해당 쿠폰을 유저에게 발급합니다.<br>" +
            "발급받을 쿠폰의 상태가 '발급 중' 상태가 아니거나 발급받은 적 있을 경우 해당 쿠폰을 발급받을 수 없습니다.")
    @ApiResponse(responseCode = "200", description = "유저의 새 쿠폰이 생성되었습니다.",
        content = @Content(schema = @Schema(implementation = UserCouponGetResponse.class)))
    @PostMapping
    public ResponseEntity<UserCouponIssueResponse> issueCoupon(@Parameter(hidden = true) @CheckLogin LoginUserRequest loginUserRequest,
                                                               @RequestBody UserCouponIssueRequest request);

    @Operation(summary = "유저의 쿠폰 사용 API", description = "유저가 해당 쿠폰의 ID를 받아 사용합니다.<br>" +
            "쿠폰 사용 예시 로직이며, 쿠폰만을 사용하는 컨트롤러 없이 다른 서비스에 사용될 수 있습니다.")
    @ApiResponse(responseCode = "204", description = "해당 유저가 보유한 쿠폰을 사용했습니다.")
    @PostMapping("/use")
    public ResponseEntity<Void> useCoupon(@Parameter(hidden = true) @CheckLogin LoginUserRequest loginUserRequest,
                                          @RequestBody UserCouponUseRequest request);

    @Operation(summary = "유저 쿠폰 만료 스케줄러 수동 API", description = "쿠폰의 만료일을 기준으로 모든 유저의 쿠폰들의 상태를 수동 만료 처리합니다.<br>" +
            "어떤 상태이든 상관없이, 만료일이 됐을 경우 모두 상태를 만료처리 합니다.")

    @PostMapping("/scheduler")
    public void schedulerExpiredUserCoupon();

}
