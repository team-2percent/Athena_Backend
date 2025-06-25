package goorm.athena.domain.notification.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FcmNotificationListener {
    private final FcmEventService fcmEventService;

    @Async
    @EventListener
    public void handleLoginNotification(FcmLoginEvent event){
        fcmEventService.notifyLogin(event.userId(), event.description());
    }

    @Async
    @EventListener
    public void handleBuyNotification(FcmPurchaseEvent event){
        fcmEventService.notifyPurchase(event.buyerId(), event.sellerId(), event.buyerName());
    }

    @Async
    @EventListener
    public void handleReviewNotification(FcmReviewEvent event){
        fcmEventService.notifyReview(event.userId(), event.projectTitle());
    }

    @Async
    @EventListener
    public void handleCouponNotification(FcmCouponEvent event){
        fcmEventService.notifyCoupon(event.couponTitle());
    }
}
