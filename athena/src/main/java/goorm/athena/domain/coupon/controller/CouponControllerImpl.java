package goorm.athena.domain.coupon.controller;

import goorm.athena.domain.coupon.dto.req.CouponCreateRequest;
import goorm.athena.domain.coupon.dto.res.CouponCreateResponse;
import goorm.athena.domain.coupon.scheduler.CouponScheduler;
import goorm.athena.domain.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/coupon")
public class CouponControllerImpl implements CouponController{
    private final CouponService couponService;
    private final CouponScheduler couponScheduler;

    @Override
    @PostMapping("/scheduler")
    public void scheduleUpdateCoupon(){
        couponScheduler.updateCouponStatuses();
    }
}
