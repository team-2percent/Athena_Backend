package goorm.athena.domain.userCoupon.entity;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.repository.CouponRepository;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.util.TestEntityFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static goorm.athena.domain.userCoupon.entity.Status.EXPIRED;
import static goorm.athena.domain.userCoupon.entity.Status.UNUSED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UserCouponTest {

    @DisplayName("특정 유저의 쿠폰을 '만료' 상태로 전환한다.")
    @Test
    void setExpired() {
        User user = new User();
        Coupon coupon = new Coupon();
        UserCoupon userCoupon = setupUserCoupon(user, coupon, UNUSED);
        userCoupon.setExpired();

        assertThat(userCoupon.getStatus()).isEqualTo(EXPIRED);
    }

    protected UserCoupon setupUserCoupon(User user, Coupon coupon, Status status){
        return TestEntityFactory.createUserCoupon(user, coupon, status);
    }

}