package goorm.athena.domain.couponEvent.mapper;

import goorm.athena.domain.couponEvent.dto.res.CouponEventCreateResponse;
import goorm.athena.domain.couponEvent.dto.res.CouponEventGetResponse;
import goorm.athena.domain.couponEvent.entity.CouponEvent;

public class CouponEventMapper {
    public static CouponEventCreateResponse toCreateResponse(CouponEvent couponEvent){
        return new CouponEventCreateResponse(
                couponEvent.getId(),
                couponEvent.getCoupon().getId(),
                couponEvent.getCoupon().getTitle(),
                couponEvent.getCoupon().getContent(),
                couponEvent.isActive(),
                couponEvent.getCoupon().getStartAt(),
                couponEvent.getCoupon().getEndAt()
        );
    }
    public static CouponEventGetResponse toGetResponse(CouponEvent couponEvent, boolean userIssued){
        return new CouponEventGetResponse(
                couponEvent.getId(),
                couponEvent.getCoupon().getId(),
                couponEvent.getCoupon().getTitle(),
                couponEvent.getCoupon().getContent(),
                couponEvent.getCoupon().getStock(),
                couponEvent.getCoupon().getExpiresAt(),
                userIssued
        );
    }
}
