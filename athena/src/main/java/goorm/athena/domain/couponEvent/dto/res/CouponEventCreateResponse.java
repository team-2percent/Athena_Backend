package goorm.athena.domain.couponEvent.dto.res;

import java.time.LocalDateTime;

public record CouponEventCreateResponse(
        Long id,
        Long couponId,
        String title,
        String content,
        boolean isActive,
        LocalDateTime startAt,
        LocalDateTime endAt
) {
}
