package goorm.athena.domain.notification.service;

import goorm.athena.domain.notification.event.FcmCouponEvent;
import goorm.athena.domain.notification.event.FcmPurchaseEvent;
import goorm.athena.domain.notification.event.FcmLoginEvent;
import goorm.athena.domain.notification.event.FcmReviewEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ApplicationEventPublisher eventPublisher;

    // 로그인 시
    public void notifyLogin(Long userId, String userName) {
        eventPublisher.publishEvent(new FcmLoginEvent(userId, userName));
    }

    // 프로젝트 결제 시
    public void notifyPurchase(Long buyerId, Long sellerId, String buyerName){
        eventPublisher.publishEvent(new FcmPurchaseEvent(buyerId, sellerId, buyerName));
    }

    // 리뷰 작성 시
    public void notifyReview(Long sellerId, String projectTitle){
        eventPublisher.publishEvent(new FcmReviewEvent(sellerId, projectTitle));
    }

    // 쿠폰 발행 시
    public void notifyCoupon(String couponTitle){
        eventPublisher.publishEvent(new FcmCouponEvent(couponTitle));
    }


}
