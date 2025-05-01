package goorm.athena.domain.order.entity;

public enum Status {
    PENDING,    // 결제 대기 상태
    PAID,       // 결제 완료 상태
    CANCELLED,  // 주문 취소 상태
    FAILED;     // 결제 실패 상태
}