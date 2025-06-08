package goorm.athena.domain.coupon.util;

import goorm.athena.domain.coupon.controller.CouponController;
import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.coupon.repository.CouponRepository;
import goorm.athena.domain.coupon.service.CouponService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.util.IntegrationControllerTestSupport;
import goorm.athena.util.TestEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public abstract class CouponControllerIntegrationSupport extends IntegrationControllerTestSupport {
    @Autowired
    protected CouponController controller;

    @Autowired
    protected CouponService couponService;

    @Autowired
    protected CouponRepository couponRepository;

    @Autowired
    protected UserRepository userRepository;

    protected User setupUser(String email, String password, String nickname, ImageGroup imageGroup) {
        User user = TestEntityFactory.createUser(email, password, nickname, imageGroup, Role.ROLE_USER);
        return user;
    }

    protected Coupon setupCoupon(String title, String content, int price, LocalDateTime startAt,
                                 LocalDateTime endAt, LocalDateTime expiresAt, int stock, CouponStatus couponStatus){
        return TestEntityFactory.createCoupon(title, content, price, startAt, endAt, expiresAt, stock, couponStatus);
    }
}
