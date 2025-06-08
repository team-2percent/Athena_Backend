package goorm.athena.domain.coupon.controller;

import goorm.athena.domain.coupon.dto.req.CouponCreateRequest;
import goorm.athena.domain.coupon.dto.res.CouponCreateResponse;
import goorm.athena.domain.coupon.dto.res.CouponEventGetResponse;
import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.coupon.util.CouponControllerIntegrationSupport;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.jwt.util.LoginUserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CouponControllerImplTest extends CouponControllerIntegrationSupport {

    @DisplayName("관리자 유저가 입력한 내용으로 쿠폰을 생성합니다.")
    @Test
    void createCouponEvent() {
        // given
        User user = setupUser("123", "123", "123", null);
        userRepository.save(user);

        CouponCreateRequest request = new CouponCreateRequest("123", "121231231233", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000);

        // when
        ResponseEntity<CouponCreateResponse> response = controller.createCouponEvent(request);

       // then
       assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
       assertThat(response.getBody().title()).isEqualTo(request.title());
       assertThat(response.getBody().content()).isEqualTo(request.content());
    }

    @Test
    void getCouponInProgress() {
        // given
        User user = setupUser("123", "123", "123", null);
        userRepository.save(user);

        LoginUserRequest loginUserRequest = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);

        Coupon coupon = setupCoupon("123", "123123123123", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000, CouponStatus.PREVIOUS);
        Coupon coupon2 = setupCoupon("1234", "1231231231234", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000, CouponStatus.IN_PROGRESS);
        Coupon coupon3 = setupCoupon("12345", "1231231231234", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000, CouponStatus.IN_PROGRESS);
        Coupon coupon4 = setupCoupon("12346", "1231231231234", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000, CouponStatus.ENDED);
        Coupon coupon5 = setupCoupon("12347", "1231231231234", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000, CouponStatus.COMPLETED);
        couponRepository.saveAll(List.of(coupon, coupon2, coupon3, coupon4, coupon5));

        // when
        ResponseEntity<List<CouponEventGetResponse>> response = controller.getCouponInProgress(loginUserRequest);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getFirst().title()).isEqualTo(coupon3.getTitle());
        assertThat(response.getBody().getFirst().content()).isEqualTo(coupon3.getContent());
        assertThat(response.getBody().getLast().title()).isEqualTo(coupon2.getTitle());
        assertThat(response.getBody().getLast().content()).isEqualTo(coupon2.getContent());
    }

    @Test
    void scheduleUpdateCoupon() {
        controller.scheduleUpdateCoupon();
    }
}