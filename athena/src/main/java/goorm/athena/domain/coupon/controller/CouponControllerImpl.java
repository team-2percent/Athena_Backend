package goorm.athena.domain.coupon.controller;

import goorm.athena.domain.coupon.dto.req.CouponCreateRequest;
import goorm.athena.domain.coupon.dto.res.CouponCreateResponse;
import goorm.athena.domain.coupon.dto.res.CouponGetDetailResponse;
import goorm.athena.domain.coupon.dto.res.CouponGetResponse;
import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
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
    private final CouponScheduler couponScheduler;

    @Override
    @PostMapping("/scheduler")
    public void scheduleUpdateCoupon(){
        couponScheduler.updateCouponStatuses();
    }
}
