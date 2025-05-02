package goorm.athena.domain.payment.service;

import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.order.repository.OrderRepository;
import goorm.athena.domain.payment.dto.req.PaymentApproveRequest;
import goorm.athena.domain.payment.dto.req.PaymentReadyRequest;
import goorm.athena.domain.payment.dto.res.KakaoPayApproveResponse;
import goorm.athena.domain.payment.dto.res.KakaoPayReadyResponse;
import goorm.athena.domain.payment.entity.Payment;
import goorm.athena.domain.payment.repository.PaymentRepository;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final KakaoPayService kakaoPayService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    public KakaoPayReadyResponse readyPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
        User user = order.getUser();

        PaymentReadyRequest requestDto = PaymentReadyRequest.from(order);

        ResponseEntity<KakaoPayReadyResponse> response = kakaoPayService.requestKakaoPayment(requestDto, user, orderId);
        String tid = response.getBody().tid();

        Payment payment = Payment.create(order, user, tid, order.getTotalPrice());
        paymentRepository.save(payment);

        return new KakaoPayReadyResponse(response.getBody().next_redirect_pc_url(), tid);
    }

    public KakaoPayApproveResponse approvePayment(String pgToken, Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        User user = payment.getUser();
        PaymentApproveRequest requestDto = new PaymentApproveRequest(orderId, pgToken);

        KakaoPayApproveResponse response = kakaoPayService.approveKakaoPayment(payment.getTid(), requestDto, user);

        payment.approve(pgToken);

        return response;
    }
}