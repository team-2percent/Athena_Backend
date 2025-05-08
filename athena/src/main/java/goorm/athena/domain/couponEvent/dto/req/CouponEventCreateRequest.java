package goorm.athena.domain.couponEvent.dto.req;

public record CouponEventCreateRequest(
        Long couponId,
        String title,
        String content
) {
}
