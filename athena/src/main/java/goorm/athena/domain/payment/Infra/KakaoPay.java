package goorm.athena.domain.payment.Infra;
import goorm.athena.domain.payment.dto.req.KakaoPayApproveRequest;
import goorm.athena.domain.payment.dto.req.PaymentReadyRequest;
import goorm.athena.domain.payment.dto.res.KakaoPayApproveResponse;
import goorm.athena.domain.payment.dto.res.KakaoPayReadyResponse;
import goorm.athena.domain.payment.event.KakaoPayApproveEvent;
import goorm.athena.domain.payment.event.KakaoPayApproveEvent2;
import goorm.athena.domain.user.entity.User;

public interface KakaoPay {

    KakaoPayReadyResponse requestKakaoPayment(PaymentReadyRequest requestDto, User user, Long orderId);
    KakaoPayApproveResponse approveKakaoPayment(KakaoPayApproveEvent event);
    KakaoPayApproveResponse approveKakaoPayment(KakaoPayApproveEvent2 event);

}
