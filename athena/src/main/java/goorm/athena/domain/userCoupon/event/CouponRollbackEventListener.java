package goorm.athena.domain.userCoupon.event;

import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CouponRollbackEventListener {

    private static final int MAX_RETRY = 5;

    private final RedissonClient redissonClient;

    public CouponRollbackEventListener(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    private static final String LUA_ROLLBACK_SCRIPT = """
        local rollbackFlagKey = KEYS[2]
        if redis.call('EXISTS', rollbackFlagKey) == 1 then
            return -1 -- 이미 롤백 중
        end
        redis.call('SET', rollbackFlagKey, '1', 'EX', 30) -- 30초 롤백 플래그 설정
        
        local used = tonumber(redis.call('GET', KEYS[1]) or '0')
        if used <= 0 then
            redis.call('DEL', rollbackFlagKey)
            return 0  -- 원복할 재고 없음
        end
        redis.call('DECR', KEYS[1])
        redis.call('DEL', rollbackFlagKey)
        return 1  -- 원복 성공
    """;

    @Async
    @EventListener
    public void handleRollbackRequest(CouponRollbackEvent event) throws InterruptedException {
        int retryCount = 0;
        boolean success = false;
        while (retryCount < MAX_RETRY && !success) {
            try {
                rollbackRedisStock(event.couponId());
                success = true;
            } catch (Exception ex) {
                retryCount++;
                Thread.sleep(1000L * retryCount);
            }
        }
        if (!success) {
            alertAdmin("Redis rollback failed for couponId: " + event.couponId());
        }
    }

    private void rollbackRedisStock(Long couponId) {
        String usedKey = "coupon_used_" + couponId;

        RScript script = redissonClient.getScript();
        Long result = script.eval(RScript.Mode.READ_WRITE, LUA_ROLLBACK_SCRIPT, RScript.ReturnType.INTEGER, List.of(usedKey));

        if (result == null || result == 0) {
            throw new RuntimeException("Rollback failed: no stock to restore for couponId " + couponId);
        }
    }

    private void alertAdmin(String message) {
        // TODO: Slack, 이메일, SMS 등 관리자 알림 로직 구현
        System.err.println("[ADMIN ALERT] " + message);
    }
}
