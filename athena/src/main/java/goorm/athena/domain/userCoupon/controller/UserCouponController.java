package goorm.athena.domain.userCoupon.controller;

import goorm.athena.domain.userCoupon.dto.req.UserCouponCreateRequest;
import goorm.athena.domain.userCoupon.dto.res.UserCouponCreateResponse;
import goorm.athena.domain.userCoupon.entity.UserCoupon;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "UserCoupon", description = "유저 쿠폰 관련 API")
@RequestMapping("/api/userCoupon")
public interface UserCouponController {

    @Operation(summary = "유저 쿠폰 생성 API", description = "해당 쿠폰을 유저에게 발급합니다.")
    @ApiResponse(responseCode = "200", description = "유저의 새 쿠폰이 생성되었습니다.",
        content = @Content(schema = @Schema(implementation = UserCoupon.class)))
    ResponseEntity<UserCouponCreateResponse> createUserCoupon(@RequestBody UserCouponCreateRequest request);
}
