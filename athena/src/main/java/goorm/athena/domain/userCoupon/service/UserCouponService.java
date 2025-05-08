package goorm.athena.domain.userCoupon.service;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.service.CouponService;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.domain.userCoupon.dto.req.UserCouponIssueRequest;
import goorm.athena.domain.userCoupon.dto.res.UserCouponIssueResponse;
import goorm.athena.domain.userCoupon.entity.Status;
import goorm.athena.domain.userCoupon.entity.UserCoupon;
import goorm.athena.domain.userCoupon.mapper.UserCouponMapper;
import goorm.athena.domain.userCoupon.repository.UserCouponRepository;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserCouponService {
    private final UserCouponRepository userCouponRepository;
    private final UserService userService;
    private final CouponService couponService;

    @Transactional
    public UserCouponIssueResponse issueCoupon(Long userId, UserCouponIssueRequest request){
        User user = userService.getUser(userId);
        Coupon coupon = couponService.getCoupon(request.couponId());
        // 1. 이미 발급받은 쿠폰인지 확인
        if(userCouponRepository.existsByUserAndCoupon(user, coupon)){
            throw new CustomException(ErrorCode.ALREADY_ISSUED_COUPON);
        }

        // 2. 쿠폰 재고 확인
        if(coupon.getStock() <= 0){
            throw new CustomException(ErrorCode.COUPON_OUT_STOCK);
        }

        coupon.decreaseStock();

        UserCoupon userCoupon = UserCoupon.create(user, coupon);
        userCouponRepository.save(userCoupon);

        return UserCouponMapper.toCreateResponse(userCoupon);
    }

    // 쿠폰 사용시 해당 로직을 사용하면 됩니다 ( 로그인 한 사용자의 Id와 사용할 유저의 쿠폰 ID를 받습니다.)
    @Transactional
    public void useCoupon(Long userId, Long userCouponId){
        User user = userService.getUser(userId);
        // 다른 유저가 자신 이외의 쿠폰은 사용하지 못하도록 검증
        UserCoupon userCoupon = userCouponRepository.findByIdAndUser(userCouponId, user)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_COUPON_NOT_FOUND));

        if(userCoupon.getStatus().equals(Status.UNUSED)) {
            userCoupon.useCoupon();
        } else {
            throw new CustomException(ErrorCode.INVALID_COUPON_STATUS);
        }
    }
}
