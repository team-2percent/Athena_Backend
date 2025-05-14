package goorm.athena.domain.coupon.controller;

import goorm.athena.domain.coupon.dto.req.CouponCreateRequest;
import goorm.athena.domain.coupon.dto.res.CouponCreateResponse;
import goorm.athena.domain.coupon.dto.res.CouponGetDetailResponse;
import goorm.athena.domain.coupon.dto.res.CouponGetResponse;
import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.mapper.CouponMapper;
import goorm.athena.domain.coupon.scheduler.CouponScheduler;
import goorm.athena.domain.coupon.service.CouponService;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/coupon")
public class CouponControllerImpl implements CouponController{
    private final CouponService couponService;
    private final CouponScheduler couponScheduler;

    @Override
    @GetMapping
    public ResponseEntity<Page<CouponGetResponse>> getCouponAll(
            @CheckLogin LoginUserRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        Page<Coupon> coupons = couponService.getCoupons(page, size);
        Page<CouponGetResponse> response = coupons.map(CouponMapper::toGetResponse);

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{couponId}")
    public ResponseEntity<CouponGetDetailResponse> getCouponDetail(
            @CheckLogin LoginUserRequest request,
            @PathVariable Long couponId) {
        CouponGetDetailResponse response = couponService.getCouponDetail(couponId);
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/scheduler")
    public void scheduleUpdateCoupon(){
        couponScheduler.updateCouponStatuses();
    }
}
