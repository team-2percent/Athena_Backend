package goorm.athena.domain.userCoupon.service;

import goorm.athena.domain.userCoupon.dto.req.UserCouponIssueRequest;
import goorm.athena.domain.userCoupon.event.CouponIssueEvent;
import goorm.athena.domain.userCoupon.event.CouponSyncTriggerEvent;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RScript;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class UserCouponCommandServiceV4_7 {
    private final RedissonClient redissonClient;
    private final ApplicationEventPublisher eventPublisher;

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
    
        if used >= total then
            local setFlag = redis.call('SETNX', flagKey, '1')
            if setFlag == 1 then
                redis.call('EXPIRE', flagKey, 60)  -- 플래그 TTL 설정
                return 2  -- 마지막 쿠폰 발급 + 플래그 세팅 완료
            else
                return 2  -- 플래그 이미 세팅됨, 재고 소진 상태
            end
        end
    
        return 1  -- 정상 발급
    """;

    public void issueCoupon(Long userId, UserCouponIssueRequest request) {
        Long couponId = request.couponId();

        // 1. 중복 체크 & 등록
        boolean added = addUserToIssuedSet(couponId, userId);
        if (!added) {
            throw new CustomException(ErrorCode.ALREADY_ISSUED_COUPON);
        }

        try {
            // 2. Redis 재고 체크 및 감소 (플래그 세팅까지 Lua에서 처리)
            int result = checkAndDecreaseRedisStock(couponId);

            // 3. 발급 성공 시 이벤트 발행
            eventPublisher.publishEvent(new CouponIssueEvent(userId, couponId, "123"));

            // 4. 재고 소진 플래그가 Lua 스크립트에서 세팅되었을 경우 동기화 이벤트 발행
            if (result == 2) {
                eventPublisher.publishEvent(new CouponSyncTriggerEvent(couponId));
            }
        } catch (RuntimeException e) {
            removeUserFromIssuedSet(couponId, userId); // 롤백
            throw e;
        }
    }

    private int checkAndDecreaseRedisStock(Long couponId) {
        String totalKey = "coupon_total_" + couponId;
        String usedKey = "coupon_used_" + couponId;
        String flagKey = "coupon_sync_triggered_" + couponId;

        List<Object> keys = Arrays.asList(totalKey, usedKey, flagKey);
        RScript script = redissonClient.getScript();

        Long result = script.eval(RScript.Mode.READ_WRITE, LUA_SCRIPT, RScript.ReturnType.INTEGER, keys);

        if (result == null || result == -1) {
            throw new CustomException(ErrorCode.COUPON_NOT_FOUND);
        } else if (result == 0) {
            throw new CustomException(ErrorCode.COUPON_OUT_STOCK);
        }

        return result.intValue();
    }

    private boolean addUserToIssuedSet(Long couponId, Long userId) {
        String key = "issued_users_" + couponId;
        RSet<String> issuedSet = redissonClient.getSet(key, StringCodec.INSTANCE);
        return issuedSet.add(String.valueOf(userId));
    }

    private void removeUserFromIssuedSet(Long couponId, Long userId) {
        String key = "issued_users_" + couponId;
        RSet<String> issuedSet = redissonClient.getSet(key, StringCodec.INSTANCE);
        issuedSet.remove(String.valueOf(userId));
    }
}
