package goorm.athena.domain.coupon.event;

import goorm.athena.domain.coupon.service.CouponRollbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponRollbackEventListener {

    private final CouponRollbackService couponRollbackService;

    @Async
    @EventListener
    public void handleRollbackRequest(CouponRollbackEvent event) {
        couponRollbackService.rollback(event.couponId());
    }
}
