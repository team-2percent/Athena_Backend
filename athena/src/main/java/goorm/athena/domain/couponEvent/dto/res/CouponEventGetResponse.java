package goorm.athena.domain.couponEvent.dto.res;

import java.time.LocalDateTime;

public record CouponEventGetResponse(
        Long id,
        Long couponId,
        String title,
        String content,
        int stock,
        int price,
        LocalDateTime expiresAt,
        boolean userIssued
) {
}
