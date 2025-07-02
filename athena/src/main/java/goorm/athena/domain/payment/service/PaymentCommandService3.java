package goorm.athena.domain.payment.service;

import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.order.service.OrderCommendService;
import goorm.athena.domain.order.service.OrderQueryService;
import goorm.athena.domain.payment.Infra.V1.KakaoPayImplForMock;
import goorm.athena.domain.payment.dto.req.PaymentReadyRequest;
import goorm.athena.domain.payment.dto.res.KakaoPayReadyResponse;
import goorm.athena.domain.payment.entity.Payment;
import goorm.athena.domain.payment.entity.Status;
import goorm.athena.domain.payment.repository.PaymentRepository;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;

import goorm.athena.domain.payment.event.KakaoPayApproveEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCommandService3 {

    private final KakaoPayImplForMock kakaoPayImplForMock;
    private final OrderCommendService orderCommendService;
    private final OrderQueryService orderQueryService;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PlatformTransactionManager transactionManager;
    private final RedissonClient redissonClient;

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


    // 레디스 락
    public void approvePayment(String pgToken, Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        // 전역락이 아닌 주문 단위로 분산락
        // 동일한 주문에 대해서만 락 적용
        // 서로 다른 주문 병렬 처리
        String lockKey = "lock:approvePayment:" + orderId;
        RLock lock = redissonClient.getLock(lockKey);

        boolean acquired = false;
        try {
            // 최대 3초 대기, 10초 후 자동 해제
            acquired = lock.tryLock(3, 10, TimeUnit.SECONDS);

            if (!acquired) {
                throw new CustomException(ErrorCode.LOCK_ACQUIRE_FAILED);
            }

            TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

            try {
                orderCommendService.postPaymentProcess(orderId);
                transactionManager.commit(status);
            } catch (Exception e) {
                transactionManager.rollback(status);
                throw e;
            }

        } catch (InterruptedException e) { // 방어적 코드 : 서블릿 타임아웃,백그라운드 작업 취소 <- 이런 요청 방지
            Thread.currentThread().interrupt(); //스레드 인터럽트 상태 유지 ->
            // (이 코드가 없으면) 코드나 외부 프레임워크가 인터럽트 상태 인식 할수 없게됨
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        eventPublisher.publishEvent(new KakaoPayApproveEvent(payment, pgToken));
    }
}
