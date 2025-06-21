package goorm.athena.domain.coupon.infra;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CouponRollbackOperation {

    private final RedissonClient redissonClient;

    private static final String LUA_ROLLBACK_SCRIPT = """
        local rollbackFlagKey = KEYS[2]
        if redis.call('EXISTS', rollbackFlagKey) == 1 then
            return -1
        end
        redis.call('SET', rollbackFlagKey, '1', 'EX', 30)
        local used = tonumber(redis.call('GET', KEYS[1]) or '0')
        if used <= 0 then
            redis.call('DEL', rollbackFlagKey)
            return 0
        end
        redis.call('DECR', KEYS[1])
        redis.call('DEL', rollbackFlagKey)
        return 1
    """;

    public void rollbackRedisStock(Long couponId) {
        String usedKey = "coupon_used_" + couponId;
        String flagKey = "rollback_flag_" + couponId;

        RScript script = redissonClient.getScript();
        Long result = script.eval(
            RScript.Mode.READ_WRITE,
            LUA_ROLLBACK_SCRIPT,
            RScript.ReturnType.INTEGER,
            List.of(usedKey, flagKey)
        );

        if (result == null || result <= 0) {
            throw new RuntimeException("Redis rollback failed for couponId " + couponId);
        }
    }

    public void alertAdmin(String message) {
        // TODO: Slack, Email, SMS 등으로 알림 전송
        System.err.println("[ADMIN ALERT] " + message);
    }
}
