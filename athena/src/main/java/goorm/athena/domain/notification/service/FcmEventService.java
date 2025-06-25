package goorm.athena.domain.notification.service;

import goorm.athena.domain.notification.entity.FcmToken;
import goorm.athena.domain.notification.event.FcmCouponEvent;
import goorm.athena.domain.notification.event.FcmReviewEvent;
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
     * v1
     * @Async
     */
    @Async
    public void notifyLoginV1(Long userId, String userName) {
        sendToUser(userId, messageFactory.forLogin(userName));
    }

    /***
     * v2
     * @Async with custom executor
     */
    @Async("fcmTaskExecutor")
    public void notifyReviewV2(Long userId, String userName) {
        sendToUser(userId, messageFactory.forReview(userName));
    }

    /***
     * v3
     * @Async with custom executor + sendAsync()
     */
    // @Async("fcmTaskExecutor")
    public void notifyReviewV3(Long userId, String userName) {
        sendToUserWithAsync(userId, messageFactory.forReview(userName));
    }


    /***
     * [알림 발송]
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
