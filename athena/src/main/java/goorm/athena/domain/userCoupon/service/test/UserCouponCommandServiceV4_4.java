package goorm.athena.domain.userCoupon.service.test;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.service.CouponQueryService;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.service.UserQueryService;
import goorm.athena.domain.userCoupon.dto.req.UserCouponIssueRequest;
import goorm.athena.domain.userCoupon.dto.res.UserCouponIssueResponse;
import goorm.athena.domain.userCoupon.entity.UserCoupon;
import goorm.athena.domain.userCoupon.mapper.UserCouponMapper;
import goorm.athena.domain.userCoupon.repository.UserCouponRepository;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/*
    Lua Script 사용, 동기 처리만 하고 확장성이 떨어지며 병목 위험이 있음
    요청이 들어오면 Redis, DB 처리 모두 한 트랜잭션 흐름에서 진행되어 동기 블로킹 발생
    동기는 가장 단순하지만 확장성, 유지보수성에 취약함
 */
@Service
@RequiredArgsConstructor
public class UserCouponCommandServiceV4_4 {
    private final RedissonClient redissonClient;
    private final UserQueryService userQueryService;
    private final CouponQueryService couponQueryService;
    private final UserCouponRepository userCouponRepository;
    private final UserCouponMapper userCouponMapper;
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

    // 1) Redis 재고 감소 처리 (트랜잭션 외부)
    public void checkAndDecreaseRedisStock(Long couponId) {
        String totalKey = "coupon_total_" + couponId;
        String usedKey = "coupon_used_" + couponId;

        RScript script = redissonClient.getScript();
        List<Object> keys = Arrays.asList(totalKey, usedKey);

        Long result = script.eval(
                RScript.Mode.READ_WRITE,
                LUA_SCRIPT,
                RScript.ReturnType.INTEGER,
                keys
        );

        if (result == null || result == -1) {
            throw new CustomException(ErrorCode.COUPON_NOT_FOUND);
        } else if (result == 0) {
            throw new CustomException(ErrorCode.COUPON_OUT_STOCK);
        }
    }

    // 2) DB 트랜잭션 내 저장 및 중복 체크
    @Transactional
    public UserCouponIssueResponse saveCouponIssue(Long userId, Long couponId) {
        User user = userQueryService.getUser(userId);
        Coupon coupon = couponQueryService.getCoupon(couponId);

        try {
            UserCoupon userCoupon = UserCoupon.create(user, coupon);
            userCouponRepository.save(userCoupon);
            return userCouponMapper.toCreateResponse(userCoupon);
        } catch (DataIntegrityViolationException e) {
            // 중복 발급 예외 처리
            throw new CustomException(ErrorCode.ALREADY_ISSUED_COUPON);
        }
    }

    // 3) 전체 쿠폰 발급 플로우 (Redis 재고 먼저 처리)
    public UserCouponIssueResponse issueCoupon(Long userId, UserCouponIssueRequest request) {
        checkAndDecreaseRedisStock(request.couponId()); // Redis 처리
        UserCouponIssueResponse response = saveCouponIssue(userId, request.couponId()); // DB 저장
        return response;
    }
}