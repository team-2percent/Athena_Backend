package goorm.athena.domain.couponEvent.controller;

import goorm.athena.domain.coupon.dto.req.CouponCreateRequest;
import goorm.athena.domain.couponEvent.dto.req.CouponEventCreateRequest;
import goorm.athena.domain.couponEvent.dto.res.CouponEventCreateResponse;
import goorm.athena.domain.couponEvent.dto.res.CouponEventGetResponse;
import goorm.athena.domain.couponEvent.scheduler.CouponEventScheduler;
import goorm.athena.domain.couponEvent.service.CouponEventService;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/couponEvent")
public class CouponEventControllerImpl implements CouponEventController{
    private final CouponEventService couponEventService;
    private final CouponEventScheduler couponEventScheduler;

    @Override
    @PostMapping("/create")
    public ResponseEntity<CouponEventCreateResponse> createCouponEvent(@RequestBody CouponCreateRequest request){
        CouponEventCreateResponse response = couponEventService.createCouponEvent(request);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/getActives")
    public ResponseEntity<List<CouponEventGetResponse>> getCouponEvents(@CheckLogin LoginUserRequest request){
        List<CouponEventGetResponse> response = couponEventService.getCouponEvent(request.userId());
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/scheduler")
    public void schedulerCouponEvent(){
        couponEventScheduler.updateCouponEventStatus();
    }
}
