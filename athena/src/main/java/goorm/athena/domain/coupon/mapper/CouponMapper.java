package goorm.athena.domain.coupon.mapper;

import goorm.athena.domain.coupon.dto.res.CouponCreateResponse;
import goorm.athena.domain.coupon.dto.res.CouponEventGetResponse;
import goorm.athena.domain.coupon.dto.res.CouponGetDetailResponse;
import goorm.athena.domain.coupon.dto.res.CouponGetResponse;
import goorm.athena.domain.coupon.entity.Coupon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/*
import goorm.athena.domain.coupon.dto.res.CouponCreateResponse;
import goorm.athena.domain.coupon.dto.res.CouponEventGetResponse;
import goorm.athena.domain.coupon.dto.res.CouponGetDetailResponse;
import goorm.athena.domain.coupon.dto.res.CouponGetResponse;
import goorm.athena.domain.coupon.entity.Coupon;

public class CouponMapper {
    /*
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

    public static CouponEventGetResponse toGetEventResponse(Coupon coupon, boolean userIssued){
        return new CouponEventGetResponse(
                coupon.getId(),
                coupon.getTitle(),
                coupon.getContent(),
                coupon.getStock(),
                coupon.getPrice(),
                coupon.getExpiresAt(),
                userIssued
        );
    }
}


 */
@Mapper(componentModel = "spring")
public interface CouponMapper {

    CouponCreateResponse toCreateResponse(Coupon coupon);

    CouponGetResponse toGetResponse(Coupon coupon);

    @Mapping(target = "status", source = "couponStatus")
    CouponGetDetailResponse toGetDetailResponse(Coupon coupon);

    @Mapping(target = "userIssued", source = "alreadyIssued")
    CouponEventGetResponse toGetEventResponse(Coupon coupon, boolean alreadyIssued);

}
