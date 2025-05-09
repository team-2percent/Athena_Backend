package goorm.athena.domain.couponEvent.controller;

import goorm.athena.domain.couponEvent.dto.req.CouponEventCreateRequest;
import goorm.athena.domain.couponEvent.dto.res.CouponEventCreateResponse;
import goorm.athena.domain.couponEvent.dto.res.CouponEventGetResponse;
import goorm.athena.domain.couponEvent.entity.CouponEvent;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "CouponEvent", description = "쿠폰 이벤트 관련 API")
@RequestMapping("/api/couponEvent")
public interface CouponEventController {
    @Operation(summary = "쿠폰 이벤트 생성 API", description = "입력된 정보로 쿠폰 이벤트를 생성합니다.<br>" +
            "관리자 권한을 가진 유저(role = 'USER_ADMIN')만 새 쿠폰을 생성할 수 있습니다.")
    @ApiResponse(responseCode = "200", description = "새 쿠폰 이벤트가 생성되었습니다.",
        content = @Content(schema = @Schema(implementation = CouponEvent.class)))
    @PostMapping("/create")
    public ResponseEntity<CouponEventCreateResponse> createCouponEvent(@RequestBody CouponEventCreateRequest request);

    @Operation(summary = "쿠폰 이벤트 조회 API", description = "현재 활성화된 쿠폰 이벤트들을 조회합니다.<br>" +
            "만약 이전에 해당 유저가 쿠폰 이벤트를 발급한 적이 있을 경우 'userIssued' 상태 값으로 True/False 값을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "활성화된 쿠폰 이벤트들을 조회했습니다.",
        content = @Content(schema = @Schema(implementation = CouponEventGetResponse.class)))
    @GetMapping("/getActives")
    public ResponseEntity<List<CouponEventGetResponse>> getCouponEvents(@Parameter(hidden = true) @CheckLogin LoginUserRequest request);

    @Operation(summary = "쿠폰 이벤트 상태 전환 스케줄러 수동 API", description = "쿠폰 이벤트의 상태 전환을 수동으로 전환합니다.<br>" +
            "쿠폰 이벤트의 쿠폰 속성의 만료일, 발급일에 따라 활성화 여부를 전환합니다.")
    @ApiResponse(responseCode = "200", description = "쿠폰 이벤트 상태 전환 스케줄러가 실행되었습니다.")
    @PostMapping("/scheduler")
    public void schedulerCouponEvent();
}
