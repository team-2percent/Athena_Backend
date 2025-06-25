package goorm.athena.domain.notification.event;

public record FcmPurchaseEvent(Long buyerId, Long sellerId, String buyerName) {
}
