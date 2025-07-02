package goorm.athena.domain.coupon.event;

import goorm.athena.domain.coupon.infra.CouponSyncOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponSyncTriggerEventListener {

    private final CouponSyncOperation couponSyncOperation;

    @Async
    @EventListener
    public void handleCouponSyncTriggerEvent(CouponSyncTriggerEvent event) {
        couponSyncOperation.syncCouponStock(event.couponId());
    }
}

