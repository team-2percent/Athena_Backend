package goorm.athena.domain.settlement.entity;

public enum Status {
    PENDING,    // 처리 대기 상태
    COMPLETED,  // 정산 완료 상태
    CANCELLED,  // 정산 취소 상태
    FAILED;     // 정산 실패 상태
}