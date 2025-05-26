package goorm.athena.domain.coupon.dto.res;

import java.time.LocalDateTime;

public record CouponEventGetResponse(
        Long id,
        String title,
        String content,
        int stock,
        int price,
        LocalDateTime expiresAt,
        boolean userIssued
) {
}
