package goorm.athena.domain.deliveryinfo.controller;


import goorm.athena.domain.deliveryinfo.dto.req.DeliveryChangeStateRequest;
import goorm.athena.domain.deliveryinfo.dto.req.DeliveryInfoRequest;
import goorm.athena.domain.deliveryinfo.dto.res.DeliveryInfoResponse;
import goorm.athena.global.jwt.util.CheckLogin;
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
            @Parameter(hidden = true) @CheckLogin LoginUserRequest loginUser,
            @RequestBody DeliveryInfoRequest request
    );

    @Operation(summary = "배송지 삭제 API", description = "로그인한 사용자의 특정 배송지를 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "배송지 삭제 성공")
    @DeleteMapping("/delivery-info/{id}")
    ResponseEntity<Void> deleteDeliveryInfo(
            @Parameter(hidden = true) @CheckLogin LoginUserRequest loginUser,
            @PathVariable Long id
    );

    @Operation(summary = "배송지 목록 조회 API", description = "로그인한 사용자의 배송지 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "배송지 목록 조회 성공")
    @GetMapping("/delivery-info")
    ResponseEntity<List<DeliveryInfoResponse>> getDeliveryInfoList(
            @Parameter(hidden = true) @CheckLogin LoginUserRequest loginUser
    );

    @Operation(summary = "배송지 상태 변경 API", description = "사용자의 기본 배송지를 일반 배송지로 바꾸고 선택한 배송지를 기본 배송지로 변경합니다.")
    @ApiResponse(responseCode = "204", description = "사용자 기본 배송지 변경 성공")
    @PutMapping("/state")
    ResponseEntity<Void> changeDeliveryInfoState(
            @Parameter(hidden = true) @CheckLogin LoginUserRequest loginUser,
            @RequestBody DeliveryChangeStateRequest request);
}