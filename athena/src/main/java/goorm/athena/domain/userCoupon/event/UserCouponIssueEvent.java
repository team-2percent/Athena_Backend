package goorm.athena.domain.userCoupon.event;

public record UserCouponIssueEvent(Long userId, Long couponId, Integer luaResult) { }
