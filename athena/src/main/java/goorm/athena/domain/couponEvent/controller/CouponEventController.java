package goorm.athena.domain.couponEvent.controller;

import goorm.athena.domain.couponEvent.dto.req.CouponEventCreateRequest;
import goorm.athena.domain.couponEvent.dto.res.CouponEventCreateResponse;
import goorm.athena.domain.couponEvent.entity.CouponEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "CouponEvent", description = "쿠폰 이벤트 관련 API")
@RequestMapping("/api/couponEvent")
public interface CouponEventController {
    @Operation(summary = "쿠폰 이벤트 생성 API", description = "입력된 정보로 쿠폰 이벤트를 생성합니다.")
    @ApiResponse(responseCode = "200", description = "새 쿠폰 이벤트가 생성되었습니다.",
        content = @Content(schema = @Schema(implementation = CouponEvent.class)))
    @PostMapping
    public ResponseEntity<CouponEventCreateResponse> crearteCouponEvent(@RequestBody CouponEventCreateRequest request);
}
