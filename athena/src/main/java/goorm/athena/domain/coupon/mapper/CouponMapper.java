package goorm.athena.domain.coupon.mapper;

import goorm.athena.domain.coupon.dto.res.CouponCreateResponse;
import goorm.athena.domain.coupon.entity.Coupon;

public class CouponMapper {
    public static CouponCreateResponse toCreateResponse(Coupon coupon){
        return new CouponCreateResponse(
                coupon.getId(),
                coupon.getTitle(),
                coupon.getContent(),
                coupon.getPrice(),
                coupon.getStartAt(),
                coupon.getEndAt(),
                coupon.getExpiresAt(),
                coupon.getStock(),
                coupon.getCouponStatus()
        );
    }
}
