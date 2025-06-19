package goorm.athena.domain.userCoupon.service;

import goorm.athena.domain.coupon.service.CouponQueryService;
import goorm.athena.domain.user.service.UserQueryService;
import goorm.athena.domain.userCoupon.event.CouponIssueEvent;
import goorm.athena.domain.userCoupon.mapper.UserCouponMapper;
import goorm.athena.domain.userCoupon.repository.UserCouponRepository;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

// Lua 스크립트 내에서 SET + DECR 처리하여 Redis에선 완전 원자적
// DB 저장은 이벤트 리스너에서 동기적으로 수행함 (EventPublisher 사용)
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCouponCommandServiceV4 {
    private final RedissonClient redissonClient;  // Redis 클라이언트 주입
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void issueCoupon(Long userId, Long couponId) {
        LuaResult result = tryIssueCoupon(couponId, userId);

        switch (result) {
            case OUT_OF_STOCK -> throw new CustomException(ErrorCode.COUPON_OUT_STOCK);
            case ALREADY_ISSUED -> throw new CustomException(ErrorCode.ALREADY_ISSUED_COUPON);
            case SUCCESS -> {
                // DB 저장은 비동기 이벤트로 할려고 했으나 이벤트 입력 값이 바뀌어서 임시 주석 처리
 //               eventPublisher.publishEvent(new CouponIssueEvent(userId, couponId));
            }
        }
    }

    public enum LuaResult {
        OUT_OF_STOCK, ALREADY_ISSUED, SUCCESS
    }

    public LuaResult tryIssueCoupon(Long couponId, Long userId) {
        String stockKey = "coupon:" + couponId + ":count";
        String userSetKey = "coupon:" + couponId + ":users";

            String script = """
            local count = tonumber(redis.call('GET', KEYS[1]))
            if not count or count <= 0 then
                return 0 -- 재고 없음
            end
            
            local added = redis.call('SADD', KEYS[2], ARGV[1])
            if added == 0 then
                return 1 -- 이미 발급됨
            end
            
            redis.call('DECR', KEYS[1])
            return 2 -- 성공
            """;

        Long result = redissonClient.getScript(StringCodec.INSTANCE)
                .eval(RScript.Mode.READ_WRITE,
                        script,
                        RScript.ReturnType.VALUE,
                        Arrays.asList(stockKey, userSetKey),
                        userId.toString());

        Long resultCode = result;
        return switch (resultCode.intValue()) {
            case 0 -> LuaResult.OUT_OF_STOCK;
            case 1 -> LuaResult.ALREADY_ISSUED;
            case 2 -> LuaResult.SUCCESS;
            default -> throw new IllegalStateException("Unknown Lua result");
        };
    }
}