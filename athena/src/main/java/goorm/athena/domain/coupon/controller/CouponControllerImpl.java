package goorm.athena.domain.coupon.controller;

import goorm.athena.domain.coupon.dto.req.CouponCreateRequest;
import goorm.athena.domain.coupon.dto.res.CouponCreateResponse;
import goorm.athena.domain.coupon.dto.res.CouponEventGetResponse;
import goorm.athena.domain.coupon.scheduler.CouponScheduler;
import goorm.athena.domain.coupon.service.CouponCommandService;
import goorm.athena.domain.coupon.service.CouponQueryService;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/coupon")
public class CouponControllerImpl implements CouponController{
    private final CouponQueryService couponQueryService;
    private final CouponCommandService couponCommandService;
    private final CouponScheduler couponScheduler;

    @Override
    @PostMapping("/create")
    public ResponseEntity<CouponCreateResponse> createCouponEvent(@RequestBody CouponCreateRequest request){
        CouponCreateResponse response = couponCommandService.createCoupon(request);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/getInProgress")
    public ResponseEntity<List<CouponEventGetResponse>> getCouponInProgress(@CheckLogin LoginUserRequest request){
        List<CouponEventGetResponse> responses = couponQueryService.getCouponEvent(request.userId());
        return ResponseEntity.ok(responses);
    }

    @Override
    @PostMapping("/scheduler")
    public void scheduleUpdateCoupon(){
        couponScheduler.updateCouponStatuses();
    }

    @Override
    @PostMapping("/schedulerSync")
    public void scheduleSync(){
        couponScheduler.publishCouponSyncEvents();
    }
}
