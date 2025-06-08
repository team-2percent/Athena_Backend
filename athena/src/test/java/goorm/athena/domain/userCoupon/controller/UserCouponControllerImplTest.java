package goorm.athena.domain.userCoupon.controller;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.userCoupon.dto.req.UserCouponIssueRequest;
import goorm.athena.domain.userCoupon.dto.req.UserCouponUseRequest;
import goorm.athena.domain.userCoupon.dto.res.UserCouponIssueResponse;
import goorm.athena.domain.userCoupon.entity.Status;
import goorm.athena.domain.userCoupon.entity.UserCoupon;
import goorm.athena.domain.userCoupon.util.UserCouponControllerIntegrationSupport;
import goorm.athena.global.jwt.util.LoginUserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static goorm.athena.domain.userCoupon.entity.Status.EXPIRED;
import static org.assertj.core.api.Assertions.assertThat;

class UserCouponControllerImplTest extends UserCouponControllerIntegrationSupport {

    @DisplayName("유저가 등록된 쿠폰 들 중 '발급 중'인 쿠폰들을 발급받는다.")
    @Test
    void issueCoupon() {
        // given
        User user = setupUser("123", "123", "123", null);
        Coupon coupon = setupCoupon("123", "123123123123", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000, CouponStatus.IN_PROGRESS);

        userRepository.save(user);
        couponRepository.save(coupon);

        LoginUserRequest loginUserRequest = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);

        UserCouponIssueRequest request = new UserCouponIssueRequest(coupon.getId());

        // when
        ResponseEntity<UserCouponIssueResponse> response = controller.issueCoupon(loginUserRequest, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().title()).isEqualTo(coupon.getTitle());
        assertThat(response.getBody().content()).isEqualTo(coupon.getContent());
    }

    @DisplayName("유저가 발급받은 '미사용' 쿠폰들을 사용한다.")
    @Test
    void useCoupon() {
        // given
        User user = setupUser("123", "123", "123", null);
        Coupon coupon = setupCoupon("123", "123123123123", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000, CouponStatus.IN_PROGRESS);
        UserCoupon userCoupon = setupUserCoupon(user, coupon, Status.UNUSED);

        userRepository.save(user);
        couponRepository.save(coupon);
        userCouponRepository.save(userCoupon);

        LoginUserRequest loginUserRequest = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);

        UserCouponUseRequest request = new UserCouponUseRequest(userCoupon.getId());

        // when
        ResponseEntity<Void> response = controller.useCoupon(loginUserRequest, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userCoupon.getStatus()).isEqualTo(Status.USED);
    }

    @Test
    void schedulerExpiredUserCoupon() {
        // given

        // when
        controller.schedulerExpiredUserCoupon();

        // then
    }
}