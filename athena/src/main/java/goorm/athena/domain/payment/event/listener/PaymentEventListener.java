package goorm.athena.domain.payment.event.listener;

import goorm.athena.domain.payment.Infra.KakaoPay;
import goorm.athena.domain.payment.dto.res.KakaoPayApproveResponse;
import goorm.athena.domain.payment.entity.Payment;
import goorm.athena.domain.payment.event.KakaoPayApproveEvent;
import goorm.athena.domain.payment.repository.PaymentRepository;
import goorm.athena.domain.payment.service.V0.KakaoPayService1;

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
    private final KakaoPayService1 kakaoPayService;
    private final PaymentRepository paymentRepository;

//    @Async("asyncTaskExecutor") // 비동기 실행
    @Async
    @EventListener
    public void handleKakaoPayApproveEvent(KakaoPayApproveEvent event) {

        KakaoPayApproveResponse response = kakaoPay.approveKakaoPayment(event);
        Payment payment = event.getPayment();

        if (response.aid() != null && !response.aid().isBlank()) {
            payment.approve(event.getPgToken());
            log.info("결제 승인 성공: paymentId={}, orderId={}", payment.getId(), payment.getOrder().getId());
        } else {
            payment.failApprove();
            // rollbackStock(payment.getOrder().getId());
            log.warn("결제 승인 실패: paymentId={}, reason={}", payment.getId(), response.message());
        }
    }




}