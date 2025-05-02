package goorm.athena.domain.payment.controller;

import goorm.athena.domain.payment.dto.res.KakaoPayApproveResponse;
import goorm.athena.domain.payment.dto.res.KakaoPayReadyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payment", description = "카카오페이 결제 API")
@RequestMapping("/api/payment")
public interface PaymentController {

    @Operation(
            summary = "카카오페이 결제 준비 요청"
            , description = "주문 ID를 통해 카카오페이 결제를 준비합니다."
    )
    @ApiResponse(responseCode = "200", description = "결제 준비 성공")
    @PostMapping("/ready/{orderId}")
    ResponseEntity<KakaoPayReadyResponse> readyPayment(@PathVariable Long orderId);


    @Operation(
            summary = "카카오페이 결제 승인 요청",
            description = "카카오페이에서 결제 후 반환한 pg_token으로 결제를 승인합니다."
    )
    @ApiResponse(responseCode = "200", description = "결제 승인 성공")
    @GetMapping("/approve/{orderId}")
    ResponseEntity<KakaoPayApproveResponse> approvePayment(
            @PathVariable Long orderId,
            @RequestParam("pg_token") String pgToken
    );
}