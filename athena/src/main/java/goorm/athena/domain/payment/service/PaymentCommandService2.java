package goorm.athena.domain.payment.service;


import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.order.service.OrderCommendService;
import goorm.athena.domain.order.service.OrderQueryService;
import goorm.athena.domain.payment.Infra.V1.KakaoPayImplForMock;
import goorm.athena.domain.payment.dto.req.PaymentReadyRequest;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCommandService2 {


    private final KakaoPayImplForMock kakaoPayImplForMock;
    private final OrderCommendService orderCommendService;
    private final OrderQueryService orderQueryService;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PlatformTransactionManager transactionManager;

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
        KakaoPayReadyResponse response = kakaoPayImplForMock.requestKakaoPayment(requestDto, user, orderId);

        Payment payment = Payment.create(order, user, response.tid(), order.getTotalPrice());
        paymentRepository.save(payment);

        return response;
    }


    // 재시도 로직 추가
    public void approvePayment(String pgToken, Long orderId) {
        Payment payment = getPayment(orderId);
        int retries = 3;

        while (retries > 0) {
            TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

            try {
                orderCommendService.postPaymentProcess(orderId);
                transactionManager.commit(status);
                break;
            } catch (Exception e) {
                transactionManager.rollback(status);
                retries--;
                try {
                    Thread.sleep(100 + new Random().nextInt(100));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // 인터럽트 상태 복구
                    throw new CustomException(ErrorCode.SLEEP_THREAD);
                }

                if (retries == 0) {
                    // 최대 재시도 초과 후 실패
                    log.error("최대 재시도 초과", e);
                    throw new CustomException(ErrorCode.PAYMENT_RETRY_OVER);
                }

            }
        }

        // 재시도 로직, 결제에 문제가 없는 경우에만 진행
        eventPublisher.publishEvent(new KakaoPayApproveEvent(payment, pgToken));


    }

    @Transactional
    public void postApproveProcess(Long orderId) {
        orderCommendService.postPaymentProcess(orderId);
    }

    private Payment getPayment(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
    }
}