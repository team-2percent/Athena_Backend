package goorm.athena.domain.coupon.dto.res;

import java.util.List;

public record CouponPageResponse(
        List<CouponGetResponse> content,
        int page,
        int size,
        int totalPages,
        long totalElements
) {
}
