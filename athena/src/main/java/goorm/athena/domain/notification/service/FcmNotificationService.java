package goorm.athena.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import goorm.athena.domain.notification.entity.FcmToken;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmNotificationService {

    private final FcmMessageFactory fcmMessageFactory;
    private final FcmTokenService fcmTokenService;

    // 로그인 시
    public void notifyLogin(Long userId, String userName) {
        sendToUser(userId, fcmMessageFactory.forLogin(userName));
    }

    // 프로젝트 결제 시
    public void notifyPurchase(Long buyerId, Long sellerId, String buyerName){
        sendToUser(buyerId, fcmMessageFactory.forPurchaseBuyer());              // 구매자에게
        sendToUser(sellerId, fcmMessageFactory.forPurchaseSeller(buyerName));   // 판매자에게
    }

    // 리뷰 작성 시
    public void notifyReview(Long sellerId, String projectTitle){
        sendToUser(sellerId, fcmMessageFactory.forReview(projectTitle));
    }

    // 쿠폰 발행 시
    public void notifyCoupon(String couponTitle){
        sendToAll(fcmMessageFactory.forCoupon(couponTitle));
    }

    private void sendToAll(FcmMessageFactory.FcmMessage fcmMessage){
        List<FcmToken> allTokens = fcmTokenService.getAllToken();

        allTokens.forEach(token ->
                send(token.getToken(), fcmMessage)
        );

    }

    private void sendToUser(Long userId, FcmMessageFactory.FcmMessage fcmMessage){
        String token = fcmTokenService.getToken(userId);
        if (token != null) {
            send(token, fcmMessage);
        }
    }

    // 실제 알림 전송 로직
    private void send(String token, FcmMessageFactory.FcmMessage fcmMessage) {
        try{
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(fcmMessage.title())
                            .setBody(fcmMessage.body())
                            .build())
                    .build();

            FirebaseMessaging.getInstance().send(message);  // 알림 보내기
        } catch(FirebaseMessagingException e){
            e.printStackTrace();
            throw new CustomException(ErrorCode.FAILED_TO_SEND);
        }
    }
}
