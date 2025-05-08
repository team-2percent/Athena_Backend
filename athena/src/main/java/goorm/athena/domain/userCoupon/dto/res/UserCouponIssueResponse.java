package goorm.athena.domain.userCoupon.dto.res;

import java.time.LocalDateTime;

public record UserCouponIssueResponse(
        Long id,
        String title,
        String content,
        int price,
        LocalDateTime expiresAt
) { }
