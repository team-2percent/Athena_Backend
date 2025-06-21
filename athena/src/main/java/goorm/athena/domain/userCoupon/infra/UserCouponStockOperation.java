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
        local total = tonumber(redis.call('GET', KEYS[1]))
        local used = tonumber(redis.call('GET', KEYS[2])) or 0
        local flagKey = KEYS[3]

        if not total then
            return -1  -- 쿠폰 없음
        end

        if used >= total then
            return 0  -- 품절
        end

        redis.call('INCR', KEYS[2])
        used = used + 1

        if used == total then
            local setFlag = redis.call('SETNX', flagKey, '1')
            if setFlag == 1 then
                redis.call('EXPIRE', flagKey, 60)
                return 2  -- 마지막 쿠폰 발급 + 플래그 세팅 완료
            else
                return 2  -- 플래그 이미 세팅됨
            end
        end

        return 1  -- 정상 발급
    """;

    public int checkAndDecreaseRedisStock(Long couponId) {
        String totalKey = "coupon_total_" + couponId;
        String usedKey = "coupon_used_" + couponId;
        String flagKey = "coupon_sync_triggered_" + couponId;

        List<Object> keys = List.of(totalKey, usedKey, flagKey);
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
