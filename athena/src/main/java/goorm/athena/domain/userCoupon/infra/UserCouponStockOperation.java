package goorm.athena.domain.userCoupon.infra;

import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RScript;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserCouponStockOperation {

    private final RedissonClient redissonClient;

    private static final String LUA_SCRIPT = """
        local metaKey = KEYS[1]
        local total = tonumber(redis.call('HGET', metaKey, 'total'))
        local used = tonumber(redis.call('HGET', metaKey, 'used')) or 0

        if not total then
            return -1  -- 쿠폰 없음
        end

        if used >= total then
            return 0  -- 품절
        end

        redis.call('HINCRBY', metaKey, 'used', 1)
        used = used + 1

        if used == total then
            local isTriggered = redis.call('HGET', metaKey, 'sync_triggered')
            if isTriggered == 1 then
                redis.call('EXPIRE', metaKey, 600)
                return 2  -- 마지막 쿠폰 발급 + 플래그 세팅 완료
            else
                return 2  -- 플래그 이미 세팅됨
            end
        end

        return 1  -- 정상 발급
    """;

    public int checkAndDecreaseRedisStock(Long couponId) {
        String metaKey = "coupon_meta_" + couponId;

        List<Object> keys = List.of(metaKey);
        RScript script = redissonClient.getScript();

        Long result = script.eval(RScript.Mode.READ_WRITE, LUA_SCRIPT, RScript.ReturnType.INTEGER, keys);

        if (result == null || result == -1) {
            throw new CustomException(ErrorCode.COUPON_NOT_FOUND);
        } else if (result == 0) {
            throw new CustomException(ErrorCode.COUPON_OUT_STOCK);
        }

        return result.intValue();
    }

    public boolean addUserToIssuedSet(Long couponId, Long userId) {
        String key = "issued_users_" + couponId;
        RSet<String> issuedSet = redissonClient.getSet(key, StringCodec.INSTANCE);
        return issuedSet.add(String.valueOf(userId));
    }

    public void removeUserFromIssuedSet(Long couponId, Long userId) {
        String key = "issued_users_" + couponId;
        RSet<String> issuedSet = redissonClient.getSet(key, StringCodec.INSTANCE);
        issuedSet.remove(String.valueOf(userId));
    }
}
