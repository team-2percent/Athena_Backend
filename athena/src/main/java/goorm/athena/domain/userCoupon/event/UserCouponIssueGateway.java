package goorm.athena.domain.userCoupon.event;

import goorm.athena.domain.coupon.event.CouponRollbackEvent;
import goorm.athena.domain.userCoupon.infra.UserCouponStockOperation;
import goorm.athena.domain.userCoupon.service.UserCouponCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCouponIssueGateway {

    private final UserCouponCommandService userCouponCommandService;
    private final UserCouponStockOperation userCouponStockOperation;
    private final ApplicationEventPublisher eventPublisher;

    public void issueUserCoupon(Long userId, Long couponId, int luaResult){
        try {
            userCouponCommandService.issueUserCoupon(userId, couponId);
        } catch (Exception e){
            try{
                userCouponStockOperation.removeUserFromIssuedSet(couponId, userId);
                if(luaResult == 1 || luaResult == 2){
                    userCouponStockOperation.rollbackRedisStock(couponId);
                }
            } catch (Exception rollbackEx) {
                eventPublisher.publishEvent(new CouponRollbackEvent(couponId));
            }
        }
    }
}
