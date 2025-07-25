package goorm.athena.domain.payment.controller;

import goorm.athena.domain.notification.service.FcmNotificationService;
import goorm.athena.domain.order.service.OrderQueryService;
import goorm.athena.domain.payment.dto.HtmlTemplates;
import goorm.athena.domain.payment.dto.res.KakaoPayApproveResponse;
import goorm.athena.domain.payment.dto.res.KakaoPayReadyResponse;
import goorm.athena.domain.payment.service.PaymentCommandService3;
import goorm.athena.domain.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentControllerImpl implements PaymentController {

    private final PaymentCommandService3 paymentCommandService;
//    private final OrderQueryService orderQueryService;
//    private final FcmNotificationService fcmNotificationService;
//    private final UserQueryService userQueryService;

    @PostMapping("/ready/{orderId}")
    public ResponseEntity<KakaoPayReadyResponse> readyPayment(
            @PathVariable Long orderId
    ) {
        KakaoPayReadyResponse response = paymentCommandService.readyPayment(orderId);

        return ResponseEntity.ok(response);
    }


//    PayCommentService 이벤트 적용 전
//    @GetMapping("/approve/{orderId}")
//    public ResponseEntity<String>  approvePayment(
//            @PathVariable Long orderId,
//            @RequestParam("pg_token") String pgToken
//    ) {
//        KakaoPayApproveResponse response = paymentCommandService.approvePayment(pgToken, orderId);
//
//        if (response.tid() == null) {
//            return buildHtmlResponse(400, HtmlTemplates.kakaoFailHtml());  // 실패 시 HTML
//        }
//
////        Long sellerId = orderQueryService.getSeller(orderId);
////        Long buyerId = orderQueryService.getBuyer(orderId);
////        String buyerName = userQueryService.getUser(buyerId).getNickname();
////        fcmNotificationService.notifyPurchase(buyerId, sellerId, buyerName);
//
//        return buildHtmlResponse(200, HtmlTemplates.kakaoSuccessHtml());
//
//    }


    // PayCommentService1~3 : 이벤트 구조
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
//        fcmNotificationSe rvice.notifyPurchase(buyerId, sellerId, buyerName);

        return buildHtmlResponse(200, HtmlTemplates.kakaoSuccessHtml());
    }


    private ResponseEntity<String> buildHtmlResponse(int statusCode, String html) {
        return ResponseEntity.status(statusCode)
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}