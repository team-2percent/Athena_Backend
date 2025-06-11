package goorm.athena.domain.coupon.service;

import goorm.athena.domain.coupon.dto.req.CouponCreateRequest;
import goorm.athena.domain.coupon.dto.res.CouponCreateResponse;
import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.mapper.CouponMapper;
import goorm.athena.domain.coupon.repository.CouponRepository;
import goorm.athena.domain.userCoupon.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponCommandService {
    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    @Transactional
    public CouponCreateResponse createCoupon(CouponCreateRequest request){
        Coupon coupon = Coupon.create(request);

        couponRepository.save(coupon);

        return couponMapper.toCreateResponse(coupon);
    }
}
