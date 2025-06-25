package goorm.athena.domain.payment.service.V0;

import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.order.service.OrderCommendService;
import goorm.athena.domain.order.service.OrderQueryService;
import goorm.athena.domain.payment.dto.req.PaymentApproveRequest;
import goorm.athena.domain.payment.dto.req.PaymentReadyRequest;
import goorm.athena.domain.payment.dto.res.KakaoPayApproveResponse;
import goorm.athena.domain.payment.dto.res.KakaoPayReadyResponse;
import goorm.athena.domain.payment.entity.Payment;
import goorm.athena.domain.payment.entity.Status;
import goorm.athena.domain.payment.event.KakaoPayApproveEvent;
import goorm.athena.domain.payment.repository.PaymentRepository;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentCommandService1 {

    private final KakaoPayService1 kakaoPayService1;
    private final OrderCommendService orderCommendService;
    private final OrderQueryService orderQueryService;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public KakaoPayReadyResponse readyPayment(Long orderId) {
        Order order = orderQueryService.getById(orderId);
        User user = order.getUser();

        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
            if (payment.getStatus() == Status.APPROVED) {
                throw new CustomException(ErrorCode.ALREADY_PAYMENT_COMPLETED);
            }
            paymentRepository.delete(payment);
            paymentRepository.flush();
        });

        PaymentReadyRequest requestDto = PaymentReadyRequest.from(order);
        KakaoPayReadyResponse response = kakaoPayService1
                .requestKakaoPayment(requestDto, user, orderId);

        Payment payment = Payment.create(order, user, response.tid(), order.getTotalPrice());
        paymentRepository.save(payment);

        return response;
    }

    public void approvePayment(String pgToken, Long orderId) {
        Payment payment = getPayment(orderId);

//        postApproveProcess(orderId, pgToken);
        postApproveProcess(orderId);
        eventPublisher.publishEvent(new KakaoPayApproveEvent(payment, pgToken));
    }

    @Transactional
    public void postApproveProcess(Long orderId) {
//        Payment payment = getPayment(orderId);
        orderCommendService.postPaymentProcess(orderId); // 재고 감소, 누적 후원액 증가 등
//        payment.approve(pgToken); // 결제 상태 변경
    }

    private Payment getPayment(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
    }

}
