package goorm.athena.domain.notification.util;

import goorm.athena.domain.notification.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmitterService {
    private static final Long TIMEOUT = 60 * 60 * 1000L;    // 1시간 타임 아웃 임의 설정

    private final EmitterRepository emitterRepository;

    // Client -> Server 연결 요청에 따른 연결
    public SseEmitter connect(Long userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitterRepository.save(userId, emitter);

        // 완료 혹은 타임아웃 시 Emitter 삭제
        emitter.onCompletion(() -> emitterRepository.deleteById(userId));
        emitter.onTimeout(() -> emitterRepository.deleteById(userId));

        // 연결 직후 더미 이벤트로 연결 테스트
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        // 클라이언트가 미수신한 Event 목록 유실 예방에 대한 코드 추가 필요

        return emitter;
    }

    // 특정 사용자에게 알림 전송
    public void sendToUser(Long userId, NotificationResponse response) {
        SseEmitter emitter = emitterRepository.get(userId);
        if (emitter != null) {
            try{
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(response));
            }
            catch (IOException e){
                emitter.complete();
                emitterRepository.deleteById(userId);
            }
        }
    }

    // 모든 사용자에게 알림 전송 (쿠폰)
    public void sendToAll(List<Long> userIds, NotificationResponse response) {
        for (Long userId : userIds) {
            sendToUser(userId, response);
        }
    }
}
