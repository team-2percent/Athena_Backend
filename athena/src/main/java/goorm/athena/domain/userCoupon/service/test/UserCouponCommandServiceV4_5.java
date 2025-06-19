package goorm.athena.domain.userCoupon.service.test;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.service.CouponQueryService;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.service.UserQueryService;
import goorm.athena.domain.userCoupon.dto.req.UserCouponIssueRequest;
import goorm.athena.domain.userCoupon.dto.res.UserCouponIssueResponse;
import goorm.athena.domain.userCoupon.entity.UserCoupon;
import goorm.athena.domain.userCoupon.event.CouponIssueEvent;
import goorm.athena.domain.userCoupon.mapper.UserCouponMapper;
import goorm.athena.domain.userCoupon.repository.UserCouponRepository;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RScript;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/*
    @Async 기반 비동기 처리로 메인에서 Redis 처리 후, 서브 스레드에서 DB 처리 진행함
    비동기 처리로 병렬화가 가능하나 내부 스레드 기반으로 확장성에 한계가 존재함
    또한, Redis Set을 사용하여 Lock을 사용하지 않음으로써 Lock으로 발생하는 부하를 줄임
 */
@Service
@RequiredArgsConstructor
public class UserCouponCommandServiceV4_5 {
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

    // Redis 재고 감소 처리 (트랜잭션 외부)
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

    // DB 트랜잭션 내 저장 및 중복 체크
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

    // Redis Set 중복 체크 & 등록 메서드
    private boolean addUserToIssuedSet(Long couponId, Long userId) {
        String issuedSetKey = "issued_users_" + couponId;
        RSet<String> issuedSet = redissonClient.getSet(issuedSetKey, StringCodec.INSTANCE);
        return issuedSet.add(String.valueOf(userId));
    }

    private void removeUserFromIssuedSet(Long couponId, Long userId) {
        String issuedSetKey = "issued_users_" + couponId;
        RSet<String> issuedSet = redissonClient.getSet(issuedSetKey, StringCodec.INSTANCE);
        issuedSet.remove(String.valueOf(userId));
    }

    // 전체 쿠폰 발급 플로우 (Redis 재고 + Redis Set 중복 체크 통합)
    public UserCouponIssueResponse issueCoupon(Long userId, UserCouponIssueRequest request) {
        Long couponId = request.couponId();

        // 1) Redis Set 중복 체크 및 등록 (중복 시 예외)
        boolean added = addUserToIssuedSet(couponId, userId);
        if (!added) {
            throw new CustomException(ErrorCode.ALREADY_ISSUED_COUPON);
        }

        try {
            // 2) Redis 재고 감소 체크
            checkAndDecreaseRedisStock(couponId);

            // 3) DB 저장 (중복 예외 시 롤백)
            UserCouponIssueResponse response = saveCouponIssue(userId, couponId);

            // 4) 이벤트 발행
            eventPublisher.publishEvent(new CouponIssueEvent(userId, couponId));
            return response;
        } catch (CustomException e) {
            // Redis Set에서 userId 제거(롤백)
            removeUserFromIssuedSet(couponId, userId);
            throw e;
        } catch (RuntimeException e) {
            removeUserFromIssuedSet(couponId, userId);
            throw e;
        }
    }
}