package goorm.athena.domain.payment.controller;

import goorm.athena.domain.payment.dto.res.KakaoPayApproveResponse;
import goorm.athena.domain.payment.dto.res.KakaoPayReadyResponse;
import goorm.athena.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentControllerImpl implements PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/ready/{orderId}")
    public ResponseEntity<KakaoPayReadyResponse> readyPayment(
            @PathVariable Long orderId
    ) {
        KakaoPayReadyResponse response = paymentService.readyPayment(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/approve/{orderId}")
    public ResponseEntity<KakaoPayApproveResponse> approvePayment(
            @PathVariable Long orderId,
            @RequestParam("pg_token") String pgToken
    ) {
        KakaoPayApproveResponse response = paymentService.approvePayment(pgToken, orderId);
        return ResponseEntity.ok(response);
    }
}