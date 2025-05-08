package goorm.athena.domain.userCoupon.dto.req;

public record UserCouponCreateRequest(
        Long userId,
        Long couponId
) {
}
