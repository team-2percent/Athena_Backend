package goorm.athena.domain.userCoupon.mapper;

import goorm.athena.domain.userCoupon.dto.res.UserCouponCreateResponse;
import goorm.athena.domain.userCoupon.entity.UserCoupon;

public class UserCouponMapper {
    public static UserCouponCreateResponse toCreateResponse(UserCoupon userCoupon){
        return new UserCouponCreateResponse(
                userCoupon.getId(),
                userCoupon.getCoupon().getTitle(),
                userCoupon.getCoupon().getContent(),
                userCoupon.getCoupon().getPrice(),
                userCoupon.getCoupon().getExpiresAt()
        );
    }
}
