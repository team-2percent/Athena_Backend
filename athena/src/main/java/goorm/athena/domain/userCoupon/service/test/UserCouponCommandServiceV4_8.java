package goorm.athena.domain.userCoupon.service.test;

import goorm.athena.domain.userCoupon.dto.req.UserCouponIssueRequest;
import goorm.athena.domain.userCoupon.event.CouponIssueEvent;
import goorm.athena.domain.userCoupon.event.CouponRollbackEvent;
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

import java.util.List;

/*
 이전 4_7에 비해 Lua Script로 재고 관리를 하는 건 똑같음.
 단, 성능은 아주 약간 저하했지만 실패 시 롤백을 하는 이벤트를 추가하여 안정성을 더 확보했음.
 실패 시 재고를 원복하는 과정에서 Set, Get이 1~2회 더 발생하지만 큰 차이는 없어 부하 높은 상황을 대비함
 */
@Service
@RequiredArgsConstructor
public class UserCouponCommandServiceV4_8 {

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
                redis.call('EXPIRE', flagKey, 60)
                return 2  -- 마지막 쿠폰 발급 + 플래그 세팅 완료
            else
                return 2  -- 플래그 이미 세팅됨
            end
        end

        return 1  -- 정상 발급
    """;

    public void issueCoupon(Long userId, UserCouponIssueRequest request) {
        Long couponId = request.couponId();

        // 1. 중복 발급 체크 (Set에 userId 추가)
        boolean added = addUserToIssuedSet(couponId, userId);
        if (!added) {
            throw new CustomException(ErrorCode.ALREADY_ISSUED_COUPON);
        }

        int luaResult = -99;

        try {
            // 2. Redis 재고 감소
            luaResult = checkAndDecreaseRedisStock(couponId);

            // 4. 이벤트 발행 (비동기 알림, 상태 동기화 등)
            eventPublisher.publishEvent(new CouponIssueEvent(userId, couponId));

            // 5. 재고 소진 플래그 감지 시 동기화 이벤트 발행
            if (luaResult == 2) {
                eventPublisher.publishEvent(new CouponSyncTriggerEvent(couponId));
            }

        } catch (Exception e) {
            // 6. 실패 시 Redis 상태 롤백: issuedSet에서 userId 제거, Redis 재고 원복 처리
            removeUserFromIssuedSet(couponId, userId);
            try {
                if (luaResult == 1 || luaResult == 2) {
                    rollbackRedisStock(couponId);
                }
            }catch (Exception rollbackEx) {
                eventPublisher.publishEvent(new CouponRollbackEvent(couponId));
            }
            throw e; // 예외 재던지기
        }
    }

    private int checkAndDecreaseRedisStock(Long couponId) {
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

    private void rollbackRedisStock(Long couponId) {
        String usedKey = "coupon_used_" + couponId;
        RBucket<Integer> usedBucket = redissonClient.getBucket(usedKey);
        // Redis 사용 카운트 원복 (감소)
        usedBucket.getAndSet(usedBucket.get() - 1);
    }
}