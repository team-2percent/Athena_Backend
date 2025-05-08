package goorm.athena.domain.userCoupon.mapper;

import goorm.athena.domain.userCoupon.dto.res.UserCouponIssueResponse;
import goorm.athena.domain.userCoupon.entity.UserCoupon;

public class UserCouponMapper {
    public static UserCouponIssueResponse toCreateResponse(UserCoupon userCoupon){
        return new UserCouponIssueResponse(
                userCoupon.getId(),
                userCoupon.getCoupon().getTitle(),
                userCoupon.getCoupon().getContent(),
                userCoupon.getCoupon().getPrice(),
                userCoupon.getCoupon().getExpiresAt()
        );
    }
}
