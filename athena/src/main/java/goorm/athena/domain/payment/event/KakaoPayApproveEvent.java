package goorm.athena.domain.payment.event;

import goorm.athena.domain.payment.entity.Payment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class KakaoPayApproveEvent {
    private final Payment payment;
    private final String pgToken;
}