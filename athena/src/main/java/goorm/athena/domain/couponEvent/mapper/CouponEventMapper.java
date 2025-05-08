package goorm.athena.domain.couponEvent.mapper;

import goorm.athena.domain.couponEvent.dto.res.CouponEventCreateResponse;
import goorm.athena.domain.couponEvent.entity.CouponEvent;

public class CouponEventMapper {
    public static CouponEventCreateResponse toCreateResponse(CouponEvent couponEvent){
        return new CouponEventCreateResponse(
                couponEvent.getId(),
                couponEvent.getCoupon().getId(),
                couponEvent.getTitle(),
                couponEvent.getContent(),
                couponEvent.isActive(),
                couponEvent.getCoupon().getStartAt(),
                couponEvent.getCoupon().getEndAt()
        );
    }

}
