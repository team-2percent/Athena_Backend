package goorm.athena.domain.userCoupon.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCouponIssueEventListener {

    private final UserCouponIssueGateway userCouponIssueGateway;

    @Async
    @EventListener
    public void listenerCouponIssueEvent(UserCouponIssueEvent event) {
        userCouponIssueGateway.issueUserCoupon(event.userId(), event.couponId(), event.luaResult());
    }
}
