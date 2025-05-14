package goorm.athena.domain.coupon.service;

import goorm.athena.domain.coupon.dto.req.CouponCreateRequest;
import goorm.athena.domain.coupon.dto.res.CouponCreateResponse;
import goorm.athena.domain.coupon.dto.res.CouponGetDetailResponse;
import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.coupon.mapper.CouponMapper;
import goorm.athena.domain.coupon.repository.CouponRepository;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;

    @Transactional
    public CouponCreateResponse createCoupon(CouponCreateRequest request){
        Coupon coupon = Coupon.create(request);

        couponRepository.save(coupon);

        return CouponMapper.toCreateResponse(coupon);
    }

    @Transactional(readOnly = true)
    public Page<Coupon> getCoupons(int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        return couponRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Coupon> getCouponByStatus(int page, int size, CouponStatus status){
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        return couponRepository.findByCouponStatus(pageable, status);
    }

    @Transactional(readOnly = true)
    public CouponGetDetailResponse getCouponDetail(Long couponId){
        Coupon coupon = getCoupon(couponId);
        return CouponMapper.toGetDetailResponse(coupon);
    }

    @Transactional(readOnly = true)
    public Coupon getCoupon(Long couponId){
        return couponRepository.findById(couponId)
                .orElseThrow(() -> new CustomException(ErrorCode.COUPON_NOT_FOUND));
    }
}
