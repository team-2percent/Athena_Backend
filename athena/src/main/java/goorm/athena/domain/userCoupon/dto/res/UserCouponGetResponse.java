package goorm.athena.domain.userCoupon.dto.res;

import goorm.athena.domain.userCoupon.entity.Status;

import java.time.LocalDateTime;

public record UserCouponGetResponse(
        Long id,
        Long couponId,
        String title,
        String content,
        int price,
        int stock,
        LocalDateTime expires,
        Status status
) {
}
