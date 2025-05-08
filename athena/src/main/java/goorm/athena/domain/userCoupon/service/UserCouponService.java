package goorm.athena.domain.userCoupon.service;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.repository.CouponRepository;
import goorm.athena.domain.coupon.service.CouponService;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.domain.userCoupon.dto.req.UserCouponCreateRequest;
import goorm.athena.domain.userCoupon.dto.res.UserCouponCreateResponse;
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
    public UserCouponCreateResponse issueCoupon(Long userId, UserCouponCreateRequest request){
        User user = userService.getUser(userId);
        Coupon coupon = couponService.getCoupon(request.couponId());
        // 1. 이미 발급받은 쿠폰인지 확인
        if(userCouponRepository.existsByUserAndCoupon(user, coupon)){
            throw new CustomException(ErrorCode.ALREADY_EXIST_USER);
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

}
