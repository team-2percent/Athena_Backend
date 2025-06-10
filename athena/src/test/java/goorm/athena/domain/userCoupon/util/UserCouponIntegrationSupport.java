package goorm.athena.domain.userCoupon.util;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.coupon.repository.CouponRepository;
import goorm.athena.domain.coupon.service.CouponQueryService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.domain.user.service.UserQueryService;
import goorm.athena.domain.userCoupon.entity.Status;
import goorm.athena.domain.userCoupon.entity.UserCoupon;
import goorm.athena.domain.userCoupon.repository.UserCouponCursorRepository;
import goorm.athena.domain.userCoupon.repository.UserCouponRepository;
import goorm.athena.domain.userCoupon.service.UserCouponCommandService;
import goorm.athena.domain.userCoupon.service.UserCouponQueryService;
import goorm.athena.util.IntegrationServiceTestSupport;
import goorm.athena.util.TestEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public abstract class UserCouponIntegrationSupport extends IntegrationServiceTestSupport {
    @Autowired
    protected UserCouponQueryService userCouponQueryService;

    @Autowired
    protected UserCouponCommandService userCouponCommandService;

    @Autowired
    protected UserCouponRepository userCouponRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected UserQueryService userQueryService;

    @Autowired
    protected CouponQueryService couponQueryService;

    @Autowired
    protected CouponRepository couponRepository;

    @Autowired
    protected UserCouponCursorRepository userCouponCursorRepository;

    protected User setupUser(String email, String password, String nickname, ImageGroup imageGroup) {
        User user = TestEntityFactory.createUser(email, password, nickname, imageGroup, Role.ROLE_USER);
        return user;
    }

    protected Coupon setupCoupon(String title, String content, int price, LocalDateTime startAt,
                                 LocalDateTime endAt, LocalDateTime expiresAt, int stock, CouponStatus couponStatus){
        return TestEntityFactory.createCoupon(title, content, price, startAt, endAt, expiresAt, stock, couponStatus);
    }

    protected UserCoupon setupUserCoupon(User user, Coupon coupon, Status status){
        return TestEntityFactory.createUserCoupon(user, coupon, status);
    }
}
