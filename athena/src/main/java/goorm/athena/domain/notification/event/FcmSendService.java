package goorm.athena.domain.notification.event;

import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import goorm.athena.domain.notification.entity.FcmToken;
import goorm.athena.domain.notification.service.FcmTokenService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmSendService {

    /***
     * 실제 FCM 알림 발송 로직
     */

    // send()
    public void send(String token, MessageFactory.FcmMessage fcmMessage) {
        try{
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(fcmMessage.title())
                            .setBody(fcmMessage.body())
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);  // 알림 보내기
            log.info("[FCM SUCCESS]: token={}, response={}", token, response);
        } catch(FirebaseMessagingException e){
            log.error("[FCM ERROR] Code={}, Message={}, HttpCode={}",
                    e.getMessagingErrorCode(), e.getMessage(), e.getHttpResponse().getStatusCode());
            throw new CustomException(ErrorCode.FAILED_TO_SEND);
        } catch (Exception e) {
            log.error("[FCM UNKNOWN ERROR] {}", e.getMessage(), e); // 전체 스택까지 찍기
            throw new CustomException(ErrorCode.FAILED_TO_SEND);
        }
    }

    // sendAsync()
    public void sendAsync(String token, MessageFactory.FcmMessage fcmMessage) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(fcmMessage.title())
                            .setBody(fcmMessage.body())
                            .build())
                    .build();

            ApiFuture<String> apiFuture = FirebaseMessaging.getInstance().sendAsync(message);
            // 비동기 응답 처리
            apiFuture.addListener(() -> {
                try {
                    String response = apiFuture.get(); // 결과 기다림 (논블로킹)
                    log.info("[FCM SUCCESS] token={}, response={}", token, response);
                } catch (Exception e) {
                    log.error("[FCM ERROR - Future Fail] token={}, error={}", token, e.getMessage(), e);
                }
            }, Executors.newSingleThreadExecutor());

        } catch (Exception e) {
            log.error("[FCM UNKNOWN ERROR] token={}, message={}", token, e.getMessage(), e);
            throw new CustomException(ErrorCode.FAILED_TO_SEND);
        }
    }
}
