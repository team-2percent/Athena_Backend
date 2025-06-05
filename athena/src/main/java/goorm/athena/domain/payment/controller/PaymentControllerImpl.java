package goorm.athena.domain.payment.controller;

import goorm.athena.domain.notification.service.FcmNotificationService;
import goorm.athena.domain.order.service.OrderService;
import goorm.athena.domain.payment.dto.res.KakaoPayApproveResponse;
import goorm.athena.domain.payment.dto.res.KakaoPayReadyResponse;
import goorm.athena.domain.payment.service.PaymentService;
import goorm.athena.domain.project.service.ProjectService;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentControllerImpl implements PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final FcmNotificationService fcmNotificationService;
    private final UserService userService;

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

        Long sellerId = orderService.getSeller(orderId);
        Long buyerId = orderService.getBuyer(orderId);
        String buyerName = userService.getUser(buyerId).getNickname();
        fcmNotificationService.notifyPurchase(buyerId, sellerId, buyerName);

        return ResponseEntity.ok(response);
    }
}