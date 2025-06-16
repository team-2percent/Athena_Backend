package goorm.athena.domain.userCoupon.service;

import goorm.athena.domain.userCoupon.dto.req.UserCouponIssueRequest;
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

@Service
@RequiredArgsConstructor
public class UserCouponCommandServiceV4_6 {
    private final RedissonClient redissonClient;
    private final ApplicationEventPublisher eventPublisher;

    private static final String LUA_SCRIPT = """
        local total = tonumber(redis.call('GET', KEYS[1]))
        local used = tonumber(redis.call('GET', KEYS[2])) or 0

        if not total then
            return -1  -- 쿠폰 없음
        end

        if used >= total then
            return 0  -- 품절
        end

        redis.call('INCR', KEYS[2])
        return 1  -- 성공
    """;

    public void issueCoupon(Long userId, UserCouponIssueRequest request) {
        Long couponId = request.couponId();
        // 1. 중복 체크 & 등록
        boolean added = addUserToIssuedSet(couponId, userId);
        if (!added) {
            throw new CustomException(ErrorCode.ALREADY_ISSUED_COUPON);
        }

        try {
            // 2. Redis 재고 체크
            checkAndDecreaseRedisStock(couponId);

            // 3. 재고가 소진되었으면 동기화 이벤트 발행
            if (isStockDepleted(couponId)) {
                eventPublisher.publishEvent(new CouponSyncTriggerEvent(couponId));
            }
        } catch (RuntimeException e) {
            removeUserFromIssuedSet(couponId, userId); // 롤백
            throw e;
        }
    }

    private void checkAndDecreaseRedisStock(Long couponId) {
        String totalKey = "coupon_total_" + couponId;
        String usedKey = "coupon_used_" + couponId;

        List<Object> keys = Arrays.asList(totalKey, usedKey);
        RScript script = redissonClient.getScript();

        Long result = script.eval(RScript.Mode.READ_WRITE, LUA_SCRIPT, RScript.ReturnType.INTEGER, keys);

        if (result == null || result == -1) {
            throw new CustomException(ErrorCode.COUPON_NOT_FOUND);
        } else if (result == 0) {
            throw new CustomException(ErrorCode.COUPON_OUT_STOCK);
        }
    }

    private boolean isStockDepleted(Long couponId) {
        String totalKey = "coupon_total_" + couponId;
        String usedKey = "coupon_used_" + couponId;

        RBucket<String> totalBucket = redissonClient.getBucket(totalKey, StringCodec.INSTANCE);
        RBucket<String> usedBucket = redissonClient.getBucket(usedKey, StringCodec.INSTANCE);

        int total = Integer.parseInt(totalBucket.get());
        int used = Integer.parseInt(usedBucket.get());

        return used >= total;
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
