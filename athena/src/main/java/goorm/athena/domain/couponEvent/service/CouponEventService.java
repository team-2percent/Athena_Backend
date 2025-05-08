package goorm.athena.domain.couponEvent.service;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.repository.CouponRepository;
import goorm.athena.domain.couponEvent.dto.req.CouponEventCreateRequest;
import goorm.athena.domain.couponEvent.dto.res.CouponEventCreateResponse;
import goorm.athena.domain.couponEvent.entity.CouponEvent;
import goorm.athena.domain.couponEvent.mapper.CouponEventMapper;
import goorm.athena.domain.couponEvent.repository.CouponEventRepository;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponEventService {
    private final CouponRepository couponRepository;
    private final CouponEventRepository couponEventRepository;

    @Transactional
    public CouponEventCreateResponse createCouponEvent(CouponEventCreateRequest request){
        Coupon coupon = couponRepository.findById(request.couponId())
                .orElseThrow(() -> new CustomException(ErrorCode.COUPON_NOT_FOUND));
        CouponEvent couponEvent = CouponEvent.create(request, coupon);

        couponEventRepository.save(couponEvent);

        return CouponEventMapper.toCreateResponse(couponEvent);
    }
}
