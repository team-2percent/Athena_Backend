package goorm.athena.domain.notification.listener;

import goorm.athena.domain.notification.event.FcmCouponEvent;
import goorm.athena.domain.notification.event.FcmLoginEvent;
import goorm.athena.domain.notification.event.FcmPurchaseEvent;
import goorm.athena.domain.notification.event.FcmReviewEvent;
import goorm.athena.domain.notification.service.FcmEventService;
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
        fcmEventService.notifyLoginV1(event.userId(), event.description());
    }

    @Async
    @EventListener
    public void handleBuyNotification(FcmPurchaseEvent event){
        fcmEventService.notifyPurchase(event.buyerId(), event.sellerId(), event.buyerName());
    }

    @Async("fcmTaskExecutor")
    @EventListener
    public void handleReviewNotification(FcmReviewEvent event){
        fcmEventService.notifyReviewV3(event.userId(), event.projectTitle());
    }

    @Async
    @EventListener
    public void handleCouponNotification(FcmCouponEvent event){
        fcmEventService.notifyCoupon(event.couponTitle());
    }
}
