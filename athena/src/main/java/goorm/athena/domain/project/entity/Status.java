package goorm.athena.domain.project.entity;

public enum Status {
    ACTIVE,    // 활성 상태 (진행 중)
    COMPLETED, // 완료 상태 (목표 달성)
    CANCELLED, // 취소 상태
    FAILED;    // 실패 상태 (목표 미달성)
}