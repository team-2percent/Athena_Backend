package goorm.athena.domain.notification.event;

import goorm.athena.domain.notification.entity.FcmToken;
import goorm.athena.domain.notification.service.FcmTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FcmEventService {

    private final MessageFactory messageFactory;
    private final FcmSendService fcmSendService;
    private final FcmTokenService fcmTokenService;

    // 로그인 시
    public void notifyLogin(Long userId, String userName) {
        sendToUser(userId, messageFactory.forLogin(userName));
    }

    // 프로젝트 결제 시
    public void notifyPurchase(Long buyerId, Long sellerId, String buyerName){
        sendToUser(buyerId, messageFactory.forPurchaseBuyer());              // 구매자에게
        sendToUser(sellerId, messageFactory.forPurchaseSeller(buyerName));   // 판매자에게
    }

    // 리뷰 작성 시
    public void notifyReview(Long sellerId, String projectTitle){
        sendToUser(sellerId, messageFactory.forReview(projectTitle));
    }

    // 쿠폰 발행 시
    public void notifyCoupon(String couponTitle){
        sendToAll(messageFactory.forCoupon(couponTitle));
    }

    /***
     * [알림 발송]
     * 부하 테스트에 따른 고도화 진행
     */

    // 일괄 발송
    public void sendToAll(MessageFactory.FcmMessage fcmMessage){
        List<FcmToken> allTokens = fcmTokenService.getAllToken();

        allTokens.forEach(token ->
                fcmSendService.send(token.getToken(), fcmMessage)
        );
    }

    // 개별 발송
    public void sendToUser(Long userId, MessageFactory.FcmMessage fcmMessage){
        String token = fcmTokenService.getToken(userId);
        if (token != null) {
            fcmSendService.send(token, fcmMessage);
        }
    }

    // 개별 발송 + sendAsync
    public void sendToUserWithAsync(Long userId, MessageFactory.FcmMessage fcmMessage){
        String token = fcmTokenService.getToken(userId);
        if (token != null) {
            fcmSendService.sendAsync(token, fcmMessage);
        }
    }

}
