package goorm.athena.domain.coupon.dto.req;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CouponCreateRequest(
        @Column(length = 25, nullable = false)
        String title,

        @Size(min = 10, max = 50)
        String content,

        @Column(length = 50000)
        int price,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime expiresAt,

        @Size(min = 1, max = 1000000)
        int stock
) {
}
