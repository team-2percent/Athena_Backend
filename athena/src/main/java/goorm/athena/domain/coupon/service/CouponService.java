package goorm.athena.domain.coupon.service;

import goorm.athena.domain.coupon.dto.req.CouponCreateRequest;
import goorm.athena.domain.coupon.dto.res.CouponCreateResponse;
import goorm.athena.domain.coupon.dto.res.CouponEventGetResponse;
import goorm.athena.domain.coupon.dto.res.CouponGetDetailResponse;
import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.coupon.mapper.CouponMapper;
import goorm.athena.domain.coupon.repository.CouponRepository;
import goorm.athena.domain.userCoupon.repository.UserCouponRepository;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;

    @Transactional
    public CouponCreateResponse createCoupon(CouponCreateRequest request){
        Coupon coupon = Coupon.create(request);

        couponRepository.save(coupon);

        return CouponMapper.toCreateResponse(coupon);
    }

    @Transactional(readOnly = true)
    public Page<Coupon> getCoupons(int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return couponRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Coupon> getCouponByStatus(int page, int size, CouponStatus status){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
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

    @Transactional(readOnly = true)
    public List<CouponEventGetResponse> getCouponEvent(Long userId) {
        List<Coupon> couponEventList = couponRepository.findAllInProgressCoupons();

        List<Long> couponIds = couponEventList.stream()
                .map(event -> event.getId())
                .toList();

        Set<Long> alreadyIssuedCouponIds = userCouponRepository
                .findCouponIdsByUserIdAndCouponIdIn(userId, couponIds);

        return couponEventList.stream()
                .map(event -> {
                    boolean alreadyIssued = alreadyIssuedCouponIds.contains(event.getId());
                    return CouponMapper.toGetEventResponse(event, alreadyIssued);
                })
                .toList();

    }
}
