package goorm.athena.domain.deliveryinfo.controller;

import goorm.athena.domain.deliveryinfo.dto.req.DeliveryInfoRequest;
import goorm.athena.domain.deliveryinfo.dto.req.DeliveryInfoUpdateRequest;
import goorm.athena.domain.deliveryinfo.dto.res.DeliveryInfoResponse;
import goorm.athena.global.jwt.util.LoginUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "DeliveryInfo", description = "사용자 배송지 관련 API")
@RequestMapping("/api/delivery")
public interface DeliveryInfoController {

    @Operation(summary = "배송지 추가 API", description = "입력된 정보로 로그인한 사용자의 배송지를 추가합니다.")
    @ApiResponse(responseCode = "200", description = "배송지 추가 성공")
    @PostMapping("/delivery-info")
    ResponseEntity<Void> addDeliveryInfo(
            @Parameter(hidden = true) LoginUserRequest loginUser,
            @RequestBody DeliveryInfoRequest request
    );

    @Operation(summary = "배송지 수정 API", description = "로그인한 사용자의 기존 배송지 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "배송지 수정 성공")
    @PutMapping("/delivery-info/{id}")
    ResponseEntity<Void> updateDeliveryInfo(
            @Parameter(hidden = true) LoginUserRequest loginUser,
            @PathVariable Long id,
            @RequestBody DeliveryInfoUpdateRequest request
    );

    @Operation(summary = "배송지 삭제 API", description = "로그인한 사용자의 특정 배송지를 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "배송지 삭제 성공")
    @DeleteMapping("/delivery-info/{id}")
    ResponseEntity<Void> deleteDeliveryInfo(
            @Parameter(hidden = true) LoginUserRequest loginUser,
            @PathVariable Long id
    );

    @Operation(summary = "기본 배송지 설정 API", description = "기존 기본 배송지를 해제하고, 선택한 배송지를 기본 배송지로 설정합니다.")
    @ApiResponse(responseCode = "200", description = "기본 배송지 설정 성공")
    @PatchMapping("/delivery-info/{id}/default")
    ResponseEntity<Void> setDefaultDeliveryInfo(
            @Parameter(hidden = true) LoginUserRequest loginUser,
            @PathVariable Long id
    );

    @Operation(summary = "배송지 목록 조회 API", description = "로그인한 사용자의 배송지 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "배송지 목록 조회 성공")
    @GetMapping("/delivery-info")
    ResponseEntity<List<DeliveryInfoResponse>> getDeliveryInfoList(
            @Parameter(hidden = true) LoginUserRequest loginUser
    );
}