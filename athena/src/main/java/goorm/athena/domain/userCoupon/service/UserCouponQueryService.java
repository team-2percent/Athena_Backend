package goorm.athena.domain.userCoupon.service;

import goorm.athena.domain.coupon.service.CouponQueryService;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.service.UserQueryService;
import goorm.athena.domain.userCoupon.dto.cursor.UserCouponCursorResponse;
import goorm.athena.domain.userCoupon.dto.res.UserCouponGetResponse;
import goorm.athena.domain.userCoupon.mapper.UserCouponMapper;
import goorm.athena.domain.userCoupon.repository.UserCouponCursorRepository;
import goorm.athena.domain.userCoupon.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserCouponQueryService {
    private final UserCouponRepository userCouponRepository;
    private final UserCouponCursorRepository userCouponCursorRepository;
    private final UserQueryService userQueryService;
    private final CouponQueryService couponQueryService;
    private final UserCouponMapper userCouponMapper;


    @Transactional(readOnly = true)
    public List<UserCouponGetResponse> getUserCoupon(Long userId){
        User user = userQueryService.getUser(userId);
        return userCouponRepository.findByUser(user).stream()
                .map(userCouponMapper::toGetResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserCouponCursorResponse getUserCoupons(Long userId, Long cursorId, int size){
        return userCouponCursorRepository.getUserCouponByCursor(userId, cursorId, size);
    }

    public String getCouponTitle(Long couponId){
        return couponQueryService.getCoupon(couponId).getTitle();
    }
}
