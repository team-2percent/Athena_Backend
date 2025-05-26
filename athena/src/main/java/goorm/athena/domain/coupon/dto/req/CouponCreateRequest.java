package goorm.athena.domain.coupon.dto.req;

import java.time.LocalDateTime;

public record CouponCreateRequest(
        String title,
        String content,
        int price,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime expiresAt,
        int stock
)  {
    public static CouponCreateRequest of(
            String title,
            String content,
            int price,
            LocalDateTime startAt,
            LocalDateTime endAt,
            LocalDateTime expiresAt,
            int stock
    ) {
        return new CouponCreateRequest(title, content, price, startAt, endAt, expiresAt, stock);
    }
}
