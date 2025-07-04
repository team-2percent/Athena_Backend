package goorm.athena.domain.payment.event.listener;

import goorm.athena.domain.order.service.OrderCommendService;
import goorm.athena.domain.payment.Infra.KakaoPay;
import goorm.athena.domain.payment.dto.res.KakaoPayApproveResponse;
import goorm.athena.domain.payment.entity.Payment;
import goorm.athena.domain.payment.event.KakaoPayApproveEvent;

import goorm.athena.domain.payment.event.KakaoPayApproveEvent2;
import goorm.athena.domain.payment.repository.PaymentRepository;
import goorm.athena.domain.payment.service.PaymentApproveService;
import goorm.athena.domain.payment.service.RedisTransactionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;



@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final KakaoPay kakaoPay;
    private final OrderCommendService orderCommendService;
    private final PaymentRepository paymentRepository;
    private final RedisTransactionUtil transactionUtil;
    private final PaymentApproveService paymentApproveService;
//    @Async("asyncTaskExecutor") // 비동기 실행
//    @Async
//    @EventListener
//    public void handleKakaoPayApproveEvent(KakaoPayApproveEvent event) {
//
//        orderCommendService.syncRedisToDb(event);
//        KakaoPayApproveResponse response = kakaoPay.approveKakaoPayment(event);
//        Payment payment = event.getPayment();
//        Long orderId = payment.getOrder().getId();
//
//        if (response.aid() != null && !response.aid().isBlank()) {
//            payment.approve(event.getPgToken());
//            log.info("결제 승인 성공: paymentId={}, orderId={}", payment.getId(), payment.getOrder().getId());
//        } else {
//            payment.failApprove();
//            orderCommendService.rollbackStock(orderId);
//            log.warn("결제 승인 실패: paymentId={}, reason={}", payment.getId(), response.message());
//        }
//    }



//    @Async
//    @EventListener
//    public void handleKakaoPayApproveEvent(KakaoPayApproveEvent2 event) {
//        Payment payment = event.getPayment();
//        Long orderId = payment.getOrder().getId();
//
//        try {
//            // 1. Redis → DB 재고 동기화 + 후원금 반영
//            orderCommendService.syncRedisToDb(event.getOrderItems(), event.getDeductedStocks());
//
//            // 2. 외부 결제 승인
//            KakaoPayApproveResponse response = kakaoPay.approveKakaoPayment(event);
//
//            if (response.aid() != null && !response.aid().isBlank()) {
//                payment.approve(event.getPgToken());
//                paymentRepository.save(payment);
//
//                event.getOrderItems().forEach(item -> {
//                    String key = "product:stock:" + item.getProduct().getId();
//                    transactionUtil.getIntegerRedisTemplate().delete(key);
//                    log.info("Redis 키 삭제 성공: {}", key);
//                });
//
//                log.info("결제 승인 성공: paymentId={}, orderId={}", payment.getId(), orderId);
//            } else {
//                throw new RuntimeException("카카오페이 응답 실패: 응답이 비어 있음");
//            }
//
//        } catch (Exception e) {
//            orderCommendService.rollbackStock(orderId);
//            payment.failApprove();
//            paymentRepository.save(payment);
//            log.error("결제 승인 실패: paymentId={}, reason={}", payment.getId(), e.getMessage(), e);
//        }
//    }


    @Async
    @EventListener
    public void handleKakaoPayApproveEvent(KakaoPayApproveEvent2 event) {
        try {
            paymentApproveService.processApprove(event);
        } catch (Exception e) {
            log.error("결제 승인 실패 (event 처리 중): {}", e.getMessage(), e);
        }
    }

}