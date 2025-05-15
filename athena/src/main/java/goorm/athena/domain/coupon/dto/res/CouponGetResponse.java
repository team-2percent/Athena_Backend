package goorm.athena.domain.coupon.dto.res;

import goorm.athena.domain.coupon.entity.CouponStatus;

import java.time.LocalDateTime;

public record CouponGetResponse(
        Long id,
        String title,
        int stock,
        CouponStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime expiresAt
) { }
