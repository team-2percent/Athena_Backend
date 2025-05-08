package goorm.athena.domain.coupon.controller;

import goorm.athena.domain.coupon.dto.req.CouponCreateRequest;
import goorm.athena.domain.coupon.dto.res.CouponCreateResponse;
import goorm.athena.domain.coupon.entity.Coupon;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Coupon", description = "쿠폰 관련 API")
@RequestMapping("/api/coupon")
public interface CouponController {
    
    @Operation(summary = "쿠폰 생성 API", description = "입력된 정보로 쿠폰을 생성합니다.")
    @ApiResponse(responseCode = "200", description = "새 쿠폰 정보 생성이 성공되었습니다.",
        content = @Content(schema = @Schema(implementation = Coupon.class)))
    @PostMapping
    ResponseEntity<CouponCreateResponse> createCoupon(@RequestBody CouponCreateRequest request);
}
