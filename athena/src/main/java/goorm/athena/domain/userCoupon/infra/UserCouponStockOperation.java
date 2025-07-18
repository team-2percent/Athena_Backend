package goorm.athena.domain.userCoupon.infra;

import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RMap;
import org.redisson.api.RScript;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class UserCouponStockOperation {

    private final RedissonClient redissonClient;

    private static final String LUA_SCRIPT = """
        local metaKey = KEYS[1]
        local total = tonumber(redis.call('HGET', metaKey, 'total'))
        local used = tonumber(redis.call('HGET', metaKey, 'used')) or 0
        
        if not total then
            return -1 -- 쿠폰 없음
        end
        
        if used == total then
            return 0 -- 품절 
        end
        
        redis.call('HINCRBY', metaKey, 'used', 1)
        return 1 -- 정상 발급
    """;

    // SHA 캐시 빌드
    private String luaScriptSha;

    @PostConstruct
    public void loadScript(){
        // LuaScript를 Redis에 등록하고 SHA1 해시값으로 등록함
        RScript script = redissonClient.getScript();
        luaScriptSha = script.scriptLoad(LUA_SCRIPT);
    }

    public int checkAndDecreaseRedisStock(Long couponId) {
        String metaKey = "coupon_meta_" + couponId;
        String ttlKey = "coupon_ttl_" + couponId;
        RScript script = redissonClient.getScript();

        List<Object> keys = List.of(metaKey);
        Long result;
        // Redis 서버 재시작시, SHA 캐시가 사라지므로 try-catch를 사용하여 캐시 재 등록
        try{
            result = script.evalSha(
                    RScript.Mode.READ_WRITE,
                    luaScriptSha, // 재사용을 위해 캐싱된 LuaScript 사용
                    RScript.ReturnType.INTEGER,
                    keys
            );
        } catch (Exception e){
            if (e.getMessage().contains("NOSCRIPT")) {
                // fallback: Lua 스크립트를 다시 eval로 실행하면서 등록
                result = script.eval(
                        RScript.Mode.READ_WRITE,
                        LUA_SCRIPT,
                        RScript.ReturnType.INTEGER,
                        keys
                );
                // 재등록된 SHA 값으로 다시 저장
                luaScriptSha = script.scriptLoad(LUA_SCRIPT);
            } else {
                throw e;
            }
        }

        if (result == null || result == -1) {
            throw new CustomException(ErrorCode.COUPON_NOT_FOUND);
        } else if (result == 0) {
            throw new CustomException(ErrorCode.COUPON_OUT_STOCK);
        }

        int baseTtl = 15;
        int jitter = ThreadLocalRandom.current().nextInt(-5, 10);
        int ttlWithJitter = baseTtl + jitter;

        // TTL 키에 TTL 설정
        redissonClient.getBucket(ttlKey, StringCodec.INSTANCE)
                .set("1", ttlWithJitter, TimeUnit.SECONDS);  // TTL이 종료되면 Redis Expired Event 발생

        RMap<String, String> metaMap = redissonClient.getMap(metaKey, StringCodec.INSTANCE);
        int total = Integer.parseInt(metaMap.get("total"));
        int used = Integer.parseInt(metaMap.get("used"));
        String triggered = metaMap.getOrDefault("sync_triggered", "0");

        if (used == total && !"1".equals(triggered)) {
            metaMap.put("sync_triggered", "1");
            metaMap.expire(600, TimeUnit.SECONDS);
            return 2;
        }

        return 1;
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
