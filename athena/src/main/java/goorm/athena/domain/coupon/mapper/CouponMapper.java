package goorm.athena.domain.coupon.mapper;

import goorm.athena.domain.coupon.dto.res.CouponCreateResponse;
import goorm.athena.domain.coupon.dto.res.CouponEventGetResponse;
import goorm.athena.domain.coupon.dto.res.CouponGetDetailResponse;
import goorm.athena.domain.coupon.dto.res.CouponGetResponse;
import goorm.athena.domain.coupon.entity.Coupon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CouponMapper {

    CouponCreateResponse toCreateResponse(Coupon coupon);

    CouponGetResponse toGetResponse(Coupon coupon);

    @Mapping(target = "status", source = "couponStatus")
    CouponGetDetailResponse toGetDetailResponse(Coupon coupon);

    @Mapping(target = "userIssued", source = "alreadyIssued")
    CouponEventGetResponse toGetEventResponse(Coupon coupon, boolean alreadyIssued);
}
