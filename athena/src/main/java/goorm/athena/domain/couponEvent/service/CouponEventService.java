package goorm.athena.domain.couponEvent.service;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.repository.CouponRepository;
import goorm.athena.domain.couponEvent.dto.req.CouponEventCreateRequest;
import goorm.athena.domain.couponEvent.dto.res.CouponEventCreateResponse;
import goorm.athena.domain.couponEvent.dto.res.CouponEventGetResponse;
import goorm.athena.domain.couponEvent.entity.CouponEvent;
import goorm.athena.domain.couponEvent.mapper.CouponEventMapper;
import goorm.athena.domain.couponEvent.repository.CouponEventRepository;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.domain.userCoupon.repository.UserCouponRepository;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponEventService {
    private final CouponRepository couponRepository;
    private final CouponEventRepository couponEventRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserService userService;

    @Transactional
    public CouponEventCreateResponse createCouponEvent(CouponEventCreateRequest request) {
        Coupon coupon = couponRepository.findById(request.couponId())
                .orElseThrow(() -> new CustomException(ErrorCode.COUPON_NOT_FOUND));
        CouponEvent couponEvent = CouponEvent.create(request, coupon);

        couponEventRepository.save(couponEvent);

        return CouponEventMapper.toCreateResponse(couponEvent);
    }

    @Transactional(readOnly = true)
    public List<CouponEventGetResponse> getCouponEvent(Long userId){
        List<CouponEvent> couponEventList = couponEventRepository.findByIsActiveTrueWithCoupon();

        List<Long> couponIds = couponEventList.stream()
                .map(event -> event.getCoupon().getId())
                .toList();

        Set<Long> alreadyIssuedCouponIds = userCouponRepository
                .findCouponIdsByUserIdAndCouponIdIn(userId, couponIds);

        return couponEventList.stream()
                .map(event -> {
                    boolean alreadyIssued = alreadyIssuedCouponIds.contains(event.getCoupon().getId());
                    return CouponEventMapper.toGetResponse(event, alreadyIssued);
                })
                .toList();

    }
}
