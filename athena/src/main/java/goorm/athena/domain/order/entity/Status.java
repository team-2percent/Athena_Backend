package goorm.athena.domain.order.entity;

public enum Status {
    ORDERED,      // 주문 완료 + 결제 완료
    CANCELED,     // 주문 취소됨
    DELIVERED     // 배송 완료
}