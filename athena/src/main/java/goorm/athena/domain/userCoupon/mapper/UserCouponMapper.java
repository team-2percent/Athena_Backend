package goorm.athena.domain.userCoupon.mapper;

import goorm.athena.domain.userCoupon.dto.cursor.UserCouponCursorResponse;
import goorm.athena.domain.userCoupon.dto.res.UserCouponGetResponse;
import goorm.athena.domain.userCoupon.dto.res.UserCouponIssueResponse;
import goorm.athena.domain.userCoupon.entity.UserCoupon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper(componentModel = "spring")
public interface UserCouponMapper {

    @Mapping(target = "title", source = "userCoupon.coupon.title")
    @Mapping(target = "content", source = "userCoupon.coupon.content")
    @Mapping(target = "price", source = "userCoupon.coupon.price")
    @Mapping(target = "expiresAt", source = "userCoupon.coupon.expiresAt")
    UserCouponIssueResponse toCreateResponse(UserCoupon userCoupon);

    @Mapping(target = "title", source = "userCoupon.coupon.title")
    @Mapping(target = "content", source = "userCoupon.coupon.content")
    @Mapping(target = "price", source = "userCoupon.coupon.price")
    @Mapping(target = "stock", source = "userCoupon.coupon.stock")
    @Mapping(target = "expires", source = "userCoupon.coupon.expiresAt")
    UserCouponGetResponse toGetResponse(UserCoupon userCoupon);

    UserCouponCursorResponse toGetCursorResponse(List<UserCouponGetResponse> content, Long nextCouponId, Long total);
}
