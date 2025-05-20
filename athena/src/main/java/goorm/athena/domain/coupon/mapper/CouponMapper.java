package goorm.athena.domain.coupon.mapper;

import goorm.athena.domain.coupon.dto.res.CouponCreateResponse;
import goorm.athena.domain.coupon.dto.res.CouponGetDetailResponse;
import goorm.athena.domain.coupon.dto.res.CouponGetResponse;
import goorm.athena.domain.coupon.entity.Coupon;
import org.springframework.data.domain.Page;

import java.util.List;

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

    public static CouponGetResponse toGetResponse(Coupon coupon){
        return new CouponGetResponse(
                coupon.getId(),
                coupon.getTitle(),
                coupon.getStock(),
                coupon.getPrice(),
                coupon.getCouponStatus(),
                coupon.getStartAt(),
                coupon.getEndAt(),
                coupon.getExpiresAt()
        );
    }

    public static CouponGetDetailResponse toGetDetailResponse(Coupon coupon){
        return new CouponGetDetailResponse(
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
