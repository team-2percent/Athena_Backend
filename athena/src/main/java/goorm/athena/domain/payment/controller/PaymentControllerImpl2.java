package goorm.athena.domain.payment.controller;

import goorm.athena.domain.notification.service.NotificationService;
import goorm.athena.domain.order.service.OrderQueryService;
import goorm.athena.domain.payment.dto.HtmlTemplates;
import goorm.athena.domain.payment.dto.res.KakaoPayReadyResponse;
import goorm.athena.domain.payment.service.V1.PaymentCommandService2;
//import goorm.athena.domain.payment.service.V1.PaymentQueryService2;
import goorm.athena.domain.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentControllerImpl2 implements PaymentController     {

    private final PaymentCommandService2 paymentCommandService;
//    private final PaymentQueryService2 paymentQueryService;
    private final OrderQueryService orderQueryService;
    private final NotificationService notificationService;
    private final UserQueryService userQueryService;

    @PostMapping("/ready/{orderId}")
    public ResponseEntity<KakaoPayReadyResponse> readyPayment(
            @PathVariable Long orderId
    ) {
        KakaoPayReadyResponse response = paymentCommandService.readyPayment(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/approve/{orderId}")
    public ResponseEntity<String> approvePayment(
            @PathVariable Long orderId,
            @RequestParam("pg_token") String pgToken
    ) {
        try {
            paymentCommandService.approvePayment(pgToken, orderId);
        } catch (Exception e) {
            return buildHtmlResponse(400, HtmlTemplates.kakaoFailHtml());
        }

        // 알림 부분은 제외를 하고 테스트를 하자
//        Long sellerId = orderQueryService.getSeller(orderId);
//        Long buyerId = orderQueryService.getBuyer(orderId);
//        String buyerName = userQueryService.getUser(buyerId).getNickname();
        // notificationService.notifyPurchase(sellerId, buyerId, buyerName);

        return buildHtmlResponse(200, HtmlTemplates.kakaoSuccessHtml());
    }

//    @GetMapping("/status/{orderId}")
//    public ResponseEntity<String> getPaymentStatus(@PathVariable Long orderId) {
//        Payment payment = paymentQueryService.findByOrderId(orderId);
//        return ResponseEntity.ok(payment.getStatus().name());
//    }

    private ResponseEntity<String> buildHtmlResponse(int statusCode, String html) {
        return ResponseEntity.status(statusCode)
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}