package goorm.athena.domain.notification.service;

import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmSendService {

    private final Executor fcmCallBackExecutor;

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

            // FCM 비동기 발송
            ApiFuture<String> apiFuture = FirebaseMessaging.getInstance().sendAsync(message);
            apiFuture.addListener(() -> {
                try {
                    String response = apiFuture.get(); // 결과 기다림 (Blocking)
                    log.info("[FCM SUCCESS] token={}, response={}", token, response);
                } catch (Exception e) {
                    log.error("[FCM ERROR - Future Fail] token={}, error={}", token, e.getMessage(), e);
                }
            }, fcmCallBackExecutor);

        } catch (Exception e) {
            log.error("[FCM UNKNOWN ERROR] token={}, message={}", token, e.getMessage(), e);
            throw new CustomException(ErrorCode.FAILED_TO_SEND);
        }
    }
}
