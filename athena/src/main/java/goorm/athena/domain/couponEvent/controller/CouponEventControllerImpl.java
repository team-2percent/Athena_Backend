package goorm.athena.domain.couponEvent.controller;

import goorm.athena.domain.couponEvent.dto.req.CouponEventCreateRequest;
import goorm.athena.domain.couponEvent.dto.res.CouponEventCreateResponse;
import goorm.athena.domain.couponEvent.entity.CouponEvent;
import goorm.athena.domain.couponEvent.scheduler.CouponEventScheduler;
import goorm.athena.domain.couponEvent.service.CouponEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/couponEvent")
public class CouponEventControllerImpl implements CouponEventController{
    private final CouponEventService couponEventService;
    private final CouponEventScheduler couponEventScheduler;

    @Override
    @PostMapping("/create")
    public ResponseEntity<CouponEventCreateResponse> createCouponEvent(@RequestBody CouponEventCreateRequest request){
        CouponEventCreateResponse response = couponEventService.createCouponEvent(request);
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/scheduler")
    public void schedulerCouponEvent(){
        couponEventScheduler.updateCouponEventStatus();
    }
}
