package goorm.athena.domain.coupon.controller;

import goorm.athena.domain.coupon.dto.req.CouponCreateRequest;
import goorm.athena.domain.coupon.dto.res.CouponCreateResponse;
import goorm.athena.domain.coupon.dto.res.CouponEventGetResponse;
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

@Tag(name = "Coupon", description = "쿠폰 관련 API")
@RequestMapping("/api/coupon")
public interface CouponController {

    @Operation(summary = "쿠폰 생성 API", description = "입력된 정보로 쿠폰을 생성합니다.<br>" +
                    "관리자 권한을 가진 유저(role = 'ROLE_ADMIN')만 새 쿠폰을 생성할 수 있습니다.")
    @ApiResponse(responseCode = "200", description = "새 쿠폰 이벤트가 생성되었습니다.",
            content = @Content(schema = @Schema(implementation = CouponEventGetResponse.class)))
    @PostMapping("/create")
    public ResponseEntity<CouponCreateResponse> createCouponEvent(@RequestBody CouponCreateRequest request);

    @Operation(summary = "쿠폰 조회 API", description = "현재 활성화된 쿠폰들을 조회합니다.<br>" +
            "만약 이전에 해당 유저가 쿠폰 이벤트를 발급한 적이 있을 경우 'userIssued' 상태 값으로 True/False 값을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "활성화된 쿠폰 이벤트들을 조회했습니다.",
            content = @Content(schema = @Schema(implementation = CouponEventGetResponse.class)))
    @GetMapping("/getInProgress")
    public ResponseEntity<List<CouponEventGetResponse>> getCouponInProgress(@Parameter(hidden = true) @CheckLogin LoginUserRequest request);


    @Operation(summary = "쿠폰의 상태 업데이트 스케줄러 수동 API", description = "쿠폰의 상태 업데이트 변경 스케줄러를 수동으로 작동합니다.<br>" +
            "상태가 발급일, 종료일에 따라 이전 -> 진행 중으로 처리할 지와 모든 쿠폰들을 만료 처리할지 스케줄러가 실행됩니다.")
    @ApiResponse(responseCode = "200", description = "쿠폰의 상태 업데이트 스케줄러 수동 API가 실행되었습니다.")
    @PostMapping("/scheduler")
    public void scheduleUpdateCoupon();

}
