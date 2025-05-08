package goorm.athena.domain.coupon.entity;

public enum CouponStatus {
    PREVIOUS, // 발급 이전
    IN_PROGRESS, // 발급 중
    COMPLETED, // 발급 완료 ( 재고 소진 )
    ENDED // 쿠폰 발급 만료
}
