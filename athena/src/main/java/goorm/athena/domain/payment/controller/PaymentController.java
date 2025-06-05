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
            summary = "카카오페이 결제 준비 요청",
            description = """
        **주문을 완료한 후, 카카오페이 결제를 시작하는 API입니다.**

        - 이 API는 `/api/orders`를 통해 생성된 주문의 `orderId`를 기반으로 호출합니다.
        - 카카오페이 결제 페이지 URL을 반환하며, 프론트는 해당 URL로 리다이렉트합니다.
        - 이후 카카오페이 결제가 완료되면 `pg_token`을 반환받아 `/api/payment/approve/{orderId}`로 승인 요청을 보내야 합니다.

        **결제 흐름 요약**
        1. `/api/orders` → 주문 생성 → `orderId` 반환
        2. `/api/payment/ready/{orderId}` → 결제 준비 및 결제 페이지 URL 응답
        3. 사용자가 카카오페이에서 결제
        4. `pg_token` 수신 → `/api/payment/approve/{orderId}?pg_token=...` 로 승인 요청
        """
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