package goorm.athena.domain.userCoupon.dto.cursor;

import goorm.athena.domain.userCoupon.dto.res.UserCouponGetResponse;

import java.util.List;

public record UserCouponCursorResponse(
        List<UserCouponGetResponse> content,
        Long nextCouponId,
        Long total
) {
    public static UserCouponCursorResponse of(
            List<UserCouponGetResponse> content,
            Long nextCouponId,
            Long total
    ) {
        return new UserCouponCursorResponse(content, nextCouponId, total);
    }
}
