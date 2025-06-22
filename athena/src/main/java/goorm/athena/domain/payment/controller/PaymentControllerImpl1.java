//package goorm.athena.domain.payment.controller;
//
//import goorm.athena.domain.notification.service.FcmNotificationService;
//import goorm.athena.domain.order.service.OrderQueryService;
//import goorm.athena.domain.payment.dto.HtmlTemplates;
//import goorm.athena.domain.payment.dto.res.KakaoPayApproveResponse;
//import goorm.athena.domain.payment.dto.res.KakaoPayReadyResponse;
//import goorm.athena.domain.payment.entity.Payment;
//import goorm.athena.domain.payment.entity.Status;
//import goorm.athena.domain.payment.service.PaymentCommandService;
//import goorm.athena.domain.payment.service.V0.PaymentCommandService1;
//import goorm.athena.domain.payment.service.V0.PaymentQueryService1;
//import goorm.athena.domain.user.service.UserQueryService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/payment")
//public class PaymentControllerImpl1 implements PaymentController {
//
//    private final PaymentCommandService1 paymentCommandService;
//    private final PaymentQueryService1 paymentQueryService1;
//    private final OrderQueryService orderQueryService;
//    private final FcmNotificationService fcmNotificationService;
//    private final UserQueryService userQueryService;
//
//    @PostMapping("/ready/{orderId}")
//    public ResponseEntity<KakaoPayReadyResponse> readyPayment(
//            @PathVariable Long orderId
//    ) {
//        KakaoPayReadyResponse response = paymentCommandService.readyPayment(orderId);
//
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/approve/{orderId}")
//    public ResponseEntity<String>  approvePayment(
//            @PathVariable Long orderId,
//            @RequestParam("pg_token") String pgToken
//    ) {
//        try {
//            paymentCommandService.approvePayment(pgToken, orderId);
//        } catch (Exception e) {
//            return buildHtmlResponse(400, HtmlTemplates.kakaoFailHtml());
//        }
//
//        Long sellerId = orderQueryService.getSeller(orderId);
//        Long buyerId = orderQueryService.getBuyer(orderId);
//        String buyerName = userQueryService.getUser(buyerId).getNickname();
//        fcmNotificationService.notifyPurchase(buyerId, sellerId, buyerName);
//
//        return buildHtmlResponse(200, HtmlTemplates.kakaoSuccessHtml());
//
//    }
//
//    private ResponseEntity<String> buildHtmlResponse(int statusCode, String html) {
//        return ResponseEntity.status(statusCode)
//                .contentType(MediaType.TEXT_HTML)
//                .body(html);
//    }
//
//
//    // 상태 확인 요청
//    @GetMapping("/status/{orderId}")
//    public ResponseEntity<String> getPaymentStatus(@PathVariable Long orderId) {
//        Payment payment = paymentQueryService1.findByOrderId(orderId); // 여기!
//        return ResponseEntity.ok(payment.getStatus().name());
//    }
//
//
//
//}