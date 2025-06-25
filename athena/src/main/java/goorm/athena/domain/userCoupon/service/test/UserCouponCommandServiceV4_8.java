package goorm.athena.domain.userCoupon.service.test;

import goorm.athena.domain.userCoupon.dto.req.UserCouponIssueRequest;
import goorm.athena.domain.userCoupon.event.UserCouponIssueEvent;
import goorm.athena.domain.coupon.event.CouponSyncTriggerEvent;
import goorm.athena.domain.userCoupon.infra.UserCouponStockOperation;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;


/*
 이전 4_7에 비해 Lua Script로 재고 관리를 하는 건 똑같음.
 단, 성능은 아주 약간 저하했지만 실패 시 롤백을 하는 이벤트를 추가하여 안정성을 더 확보했음.
 실패 시 재고를 원복하는 과정에서 Set, Get이 1~2회 더 발생하지만 큰 차이는 없어 부하 높은 상황을 대비함
 */
@Service
@RequiredArgsConstructor
public class UserCouponCommandServiceV4_8 {

    private final ApplicationEventPublisher eventPublisher;
    private final UserCouponStockOperation userCouponStockOperation;

    public void issueCoupon(Long userId, UserCouponIssueRequest request) {
        Long couponId = request.couponId();

        // 1. 중복 발급 체크 (Set에 userId 추가)
        boolean added = userCouponStockOperation.addUserToIssuedSet(couponId, userId);
        if (!added) {
            throw new CustomException(ErrorCode.ALREADY_ISSUED_COUPON);
        }

        int luaResult = -99;

        // 2. Redis 재고 감소
        luaResult = userCouponStockOperation.checkAndDecreaseRedisStock(couponId);

        // 4. 이벤트 발행 (비동기 알림, 상태 동기화 등)
        eventPublisher.publishEvent(new UserCouponIssueEvent(userId, couponId, luaResult));

        // 5. 재고 소진 플래그 감지 시 동기화 이벤트 발행
        if (luaResult == 2) {
            eventPublisher.publishEvent(new CouponSyncTriggerEvent(couponId));
        }
    }
}