package goorm.athena.domain.payment.entity;

public enum Status {
    PENDING,     // 결제 대기 상태
    APPROVED,    // 결제 승인 상태
    FAILED,      // 결제 실패 상태
    CANCELLED,   // 결제 취소 상태
    REFUNDED;    // 결제 환불 상태
}