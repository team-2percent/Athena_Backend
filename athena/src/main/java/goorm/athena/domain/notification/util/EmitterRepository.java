package goorm.athena.domain.notification.util;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// User별 Emmiter 저장소
@Repository
public class EmitterRepository {
    private  final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    // Emitter 저장
    public SseEmitter save(Long userId, SseEmitter emitter) {
        emitters.put(userId, emitter);
        return emitter;
    }

    public SseEmitter get(Long userId) {
        return emitters.get(userId);
    }

    // Emitter 삭제
    public void deleteById(Long userId) {
        emitters.remove(userId);
    }

    // Emitter를 가진 모든 UserId Get
    public List<Long> getAllUserIds() {
        return new ArrayList<>(emitters.keySet());
    }

    // 이벤트 cache 생성 및 삭제 고려 (연결을 잃을 시 이벤트 유실 방지)
}
