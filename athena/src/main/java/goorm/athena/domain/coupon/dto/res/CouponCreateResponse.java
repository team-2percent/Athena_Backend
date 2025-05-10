package goorm.athena.domain.coupon.dto.res;

import goorm.athena.domain.coupon.entity.CouponStatus;

import java.time.LocalDateTime;

public record CouponCreateResponse(
        Long id,
        Long code,
        String title,
        String content,
        int price,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime expiresAt,
        int stock,
        CouponStatus status
) {
}
