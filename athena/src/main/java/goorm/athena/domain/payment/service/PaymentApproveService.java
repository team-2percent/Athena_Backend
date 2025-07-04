package goorm.athena.domain.payment.service;


import goorm.athena.domain.order.service.OrderCommendService;
import goorm.athena.domain.payment.Infra.KakaoPay;
import goorm.athena.domain.payment.dto.res.KakaoPayApproveResponse;
import goorm.athena.domain.payment.entity.Payment;
import goorm.athena.domain.payment.event.KakaoPayApproveEvent2;
import goorm.athena.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentApproveService {

    private final KakaoPay kakaoPay;
    private final PaymentRepository paymentRepository;
    private final OrderCommendService orderCommendService;
    private final RedisTransactionUtil transactionUtil;
    private final PlatformTransactionManager transactionManager;


    public void processApprove(KakaoPayApproveEvent2 event) {


        Payment payment = event.getPayment();
        Long orderId = payment.getOrder().getId();

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            orderCommendService.syncRedisToDb(event.getOrderItems(), event.getDeductedStocks());
            KakaoPayApproveResponse response = retryApprove(event, 2);

            if (response.aid() != null && !response.aid().isBlank()) {
                payment.approve(event.getPgToken());
                paymentRepository.save(payment);

                event.getOrderItems().forEach(item -> {
                    String key = "product:stock:" + item.getProduct().getId();
                    transactionUtil.getIntegerRedisTemplate().delete(key);
                    log.info("Redis 키 삭제 성공: {}", key);
                });
                transactionManager.commit(status);
                log.info("결제 승인 성공: paymentId={}, orderId={}", payment.getId(), orderId);
            } else {
                throw new RuntimeException("카카오페이 응답 실패: 응답이 비어 있음");
            }


        } catch (Exception e) {
            transactionManager.rollback(status);
            orderCommendService.rollbackStock(orderId);
            payment.failApprove();
            paymentRepository.save(payment);
            throw new RuntimeException("결제 승인 실패: " + e.getMessage(), e);
        }
    }

    private KakaoPayApproveResponse retryApprove(KakaoPayApproveEvent2 event, int maxAttempts) {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < maxAttempts) {
            try {
                return kakaoPay.approveKakaoPayment(event);
            } catch (HttpClientErrorException e) {
                // 4xx 에러는 재시도 불가
                throw new RuntimeException("카카오페이 승인 요청 오류 (4xx): " + e.getMessage());
            } catch (ResourceAccessException | HttpServerErrorException e) {
                lastException = e;
                log.warn("카카오페이 승인 시도 {} 실패: {}", attempts + 1, e.getMessage());
                attempts++;
                try {
                    Thread.sleep(1000); // 1초 대기 후 재시도
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        throw new RuntimeException("카카오페이 결제 승인 실패 (재시도 실패): " + lastException.getMessage());
    }
}