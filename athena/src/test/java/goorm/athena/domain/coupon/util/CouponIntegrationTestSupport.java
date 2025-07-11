package goorm.athena.domain.coupon.util;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.coupon.repository.CouponRepository;
import goorm.athena.domain.coupon.service.CouponCommandService;
import goorm.athena.domain.coupon.service.CouponQueryService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.domain.user.service.UserQueryService;
import goorm.athena.domain.userCoupon.repository.UserCouponRepository;
import goorm.athena.domain.userCoupon.service.UserCouponCommandService;
import goorm.athena.domain.userCoupon.service.UserCouponQueryService;
import goorm.athena.util.IntegrationServiceTestSupport;
import goorm.athena.util.TestEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public abstract class CouponIntegrationTestSupport extends IntegrationServiceTestSupport {
    @Autowired
    protected CouponRepository couponRepository;

    @Autowired
    protected CouponCommandService couponCommandService;

    @Autowired
    protected UserCouponCommandService userCouponCommandService;

    @Autowired
    protected CouponQueryService couponQueryService;

    @Autowired
    protected UserRepository userRepository;

    protected User setupUser(String email, String password, String nickname, ImageGroup imageGroup) {
        User user = TestEntityFactory.createUser(email, password, nickname, imageGroup, Role.ROLE_USER);
        return user;
    }
}
