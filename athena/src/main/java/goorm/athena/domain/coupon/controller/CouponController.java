package goorm.athena.domain.coupon.controller;

import goorm.athena.domain.coupon.dto.req.CouponCreateRequest;
import goorm.athena.domain.coupon.dto.res.CouponCreateResponse;
import goorm.athena.domain.coupon.dto.res.CouponGetDetailResponse;
import goorm.athena.domain.coupon.dto.res.CouponGetResponse;
import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Coupon", description = "쿠폰 관련 API")
@RequestMapping("/api/coupon")
public interface CouponController {

    @Operation(summary = "쿠폰 페이지 조회 API", description = "쿠폰 목록들을 페이지 형식으로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "쿠폰 목록 페이지 조회 완료")
    @GetMapping
    public ResponseEntity<Page<CouponGetResponse>> getCouponAll(
            @Parameter(hidden = true) @CheckLogin LoginUserRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    @Operation(summary = "쿠폰 상태값 조회 API", description = "쿠폰 목록들을 상태값을 기준으로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "쿠폰 상태 기준 목록 조회 완료")
    @GetMapping("/status")
    public ResponseEntity<Page<CouponGetResponse>> getCouponByStatus(
            @Parameter(hidden = true) @CheckLogin LoginUserRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam CouponStatus status);


    @Operation(summary = "쿠폰 상세 정보 조회 API", description = "쿠폰의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "쿠폰의 상세 정보를 조회합니다.")
    @GetMapping("/{couponId}")
    public ResponseEntity<CouponGetDetailResponse> getCouponDetail(
            @Parameter(hidden = true) @CheckLogin LoginUserRequest request,
            @PathVariable Long couponId);

    @Operation(summary = "쿠폰의 상태 업데이트 스케줄러 수동 API", description = "쿠폰의 상태 업데이트 변경 스케줄러를 수동으로 작동합니다.<br>" +
            "상태가 발급일, 종료일에 따라 이전 -> 진행 중으로 처리할 지와 모든 쿠폰들을 만료 처리할지 스케줄러가 실행됩니다.")
    @ApiResponse(responseCode = "200", description = "쿠폰의 상태 업데이트 스케줄러 수동 API가 실행되었습니다.")
    @PostMapping("/scheduler")
    public void scheduleUpdateCoupon();

}
