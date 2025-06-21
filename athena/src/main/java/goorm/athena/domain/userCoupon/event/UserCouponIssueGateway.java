package goorm.athena.domain.userCoupon.event;

import goorm.athena.domain.coupon.event.CouponRollbackEvent;
import goorm.athena.domain.userCoupon.infra.UserCouponStockOperation;
import goorm.athena.domain.userCoupon.service.UserCouponCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCouponIssueGateway {

    private final UserCouponCommandService userCouponCommandService;
    private final UserCouponStockOperation userCouponStockOperation;
    private final ApplicationEventPublisher eventPublisher;

    public void issueUserCoupon(Long userId, Long couponId, int luaResult){
        try {
            userCouponCommandService.issueUserCoupon(userId, couponId);
        } catch (Exception e){
            log.error("[쿠폰 발급 실패] userId: {}, couponId: {}, 원인: {}", userId, couponId, e.getMessage(), e);
            try{
                userCouponStockOperation.removeUserFromIssuedSet(couponId, userId);
                if(luaResult == 1 || luaResult == 2){
                    eventPublisher.publishEvent(new CouponRollbackEvent(couponId));
                  //  throw new RuntimeException("유저 쿠폰 롤백 중 에러 발생");
                }
            } catch (Exception rollbackEx) {
                log.error("[Redis 롤백 실패] couponId: {}, rollbackEx: {}", couponId, rollbackEx.getMessage(), rollbackEx);
                // TODO: 알림 시스템 구현 시 아래에 관리자 알림 기능 추가
            }
            throw new RuntimeException("쿠폰 발급 중 예외 및 롤백 중 실패 발생", e);
        }
    }
}
