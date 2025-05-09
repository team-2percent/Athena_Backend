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
    
    @Operation(summary = "쿠폰 생성 API", description = "입력된 정보로 쿠폰을 생성합니다.<br>" +
            "관리자 권한을 가진 유저(role = 'USER_ADMIN')만 새 쿠폰을 생성할 수 있습니다.")
    @ApiResponse(responseCode = "200", description = "새 쿠폰 정보 생성이 성공되었습니다.",
        content = @Content(schema = @Schema(implementation = Coupon.class)))
    @PostMapping("/create")
    ResponseEntity<CouponCreateResponse> createCoupon(@RequestBody CouponCreateRequest request);

    @Operation(summary = "쿠폰의 상태 업데이트 스케줄러 수동 API", description = "쿠폰의 상태 업데이트 변경 스케줄러를 수동으로 작동합니다.<br>" +
            "상태가 발급일, 종료일에 따라 이전 -> 진행 중으로 처리할 지와 모든 쿠폰들을 만료 처리할지 스케줄러가 실행됩니다.")
    @ApiResponse(responseCode = "200", description = "쿠폰의 상태 업데이트 스케줄러 수동 API가 실행되었습니다.")
    @PostMapping("/scheduler")
    public void scheduleUpdateCoupon();
}
