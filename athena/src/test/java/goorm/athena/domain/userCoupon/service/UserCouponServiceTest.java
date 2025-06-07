package goorm.athena.domain.userCoupon.service;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.userCoupon.dto.cursor.UserCouponCursorResponse;
import goorm.athena.domain.userCoupon.dto.req.UserCouponIssueRequest;
import goorm.athena.domain.userCoupon.dto.res.UserCouponGetResponse;
import goorm.athena.domain.userCoupon.dto.res.UserCouponIssueResponse;
import goorm.athena.domain.userCoupon.entity.Status;
import goorm.athena.domain.userCoupon.entity.UserCoupon;
import goorm.athena.domain.userCoupon.util.UserCouponIntegrationSupport;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static goorm.athena.domain.userCoupon.entity.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class UserCouponServiceTest extends UserCouponIntegrationSupport {

    @DisplayName("유저가 등록된 쿠폰들 중 '진행 중'인 쿠폰을 발급받는다.")
    @Test
    void issueCoupon_Success() {
        // given
        User user = setupUser("123", "123", "123", null);
        Coupon coupon = setupCoupon("123", "123123123123", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000, CouponStatus.IN_PROGRESS);

        userRepository.save(user);
        couponRepository.save(coupon);

        UserCouponIssueRequest request = new UserCouponIssueRequest(coupon.getId());

        // when
        UserCouponIssueResponse response = userCouponService.issueCoupon(user.getId(), request);


        // then
        assertThat(response.content()).isEqualTo(coupon.getContent());
        assertThat(response.title()).isEqualTo(coupon.getTitle());
        assertThat(response.price()).isEqualTo(coupon.getPrice());
    }

    @DisplayName("유저가 '진행 중' 상태가 아닌 쿠폰을 발급받을려고 하면 에러를 리턴한다..")
    @Test
    void issueCoupon_NOT_INPROGRESS() {
        // given
        User user = setupUser("123", "123", "123", null);
        Coupon coupon = setupCoupon("123", "123123123123", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000, CouponStatus.ENDED);

        userRepository.save(user);
        couponRepository.save(coupon);

        UserCouponIssueRequest request = new UserCouponIssueRequest(coupon.getId());

        // when & thenn
        assertThatThrownBy(() -> userCouponService.issueCoupon(user.getId(), request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INVALID_COUPON_STATUS.getErrorMessage());
    }

    @DisplayName("유저가 이미 발급받은 쿠폰을 한번 더 발급받을려고 하면 에러를 리턴한다.")
    @Test
    void issueCoupon_ALREADY_ISSUED() {
        // given
        User user = setupUser("123", "123", "123", null);
        Coupon coupon = setupCoupon("123", "123123123123", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000, CouponStatus.IN_PROGRESS);

        userRepository.save(user);
        couponRepository.save(coupon);

        UserCouponIssueRequest request = new UserCouponIssueRequest(coupon.getId());

        userCouponService.issueCoupon(user.getId(), request);

        // when & thenn
        assertThatThrownBy(() -> userCouponService.issueCoupon(user.getId(), request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.ALREADY_ISSUED_COUPON.getErrorMessage());
    }

    @DisplayName("유저가 발급받은 쿠폰을 사용한다.")
    @Test
    void useCoupon_Success() {
        // given
        User user = setupUser("123", "123", "123", null);
        Coupon coupon = setupCoupon("123", "123123123123", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000, CouponStatus.IN_PROGRESS);
        UserCoupon userCoupon = setupUserCoupon(user, coupon, UNUSED);

        userRepository.save(user);
        couponRepository.save(coupon);
        userCouponRepository.save(userCoupon);

        // when
        userCouponService.useCoupon(user.getId(), userCoupon.getId());

        // then
        assertThat(userCoupon.getStatus()).isEqualTo(Status.USED);
    }

    @DisplayName("유저가 사용 할 수 없는 쿠폰을 사용하려고 하면 에러를 리턴한다.")
    @Test
    void useCoupon_INVALID_USE_COUPON() {
        // given
        User user = setupUser("123", "123", "123", null);
        Coupon coupon = setupCoupon("123", "123123123123", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000, CouponStatus.IN_PROGRESS);
        UserCoupon userCoupon = setupUserCoupon(user, coupon, EXPIRED);

        userRepository.save(user);
        couponRepository.save(coupon);
        userCouponRepository.save(userCoupon);

        // when & then
        assertThatThrownBy(() -> userCouponService.useCoupon(user.getId(), userCoupon.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INVALID_USE_COUPON.getErrorMessage());
    }

    @DisplayName("유저가 자신 이외의 쿠폰을 사용하려고 하면 에러를 리턴한다.")
    @Test
    void useCoupon_NOT_FOUND() {
        // given
        User user = setupUser("123", "123", "123", null);
        User user2 = setupUser("1234", "1234", "1234", null);
        Coupon coupon = setupCoupon("123", "123123123123", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000, CouponStatus.IN_PROGRESS);
        UserCoupon userCoupon = setupUserCoupon(user, coupon, EXPIRED);

        userRepository.saveAll(List.of(user, user2));
        couponRepository.save(coupon);
        userCouponRepository.save(userCoupon);

        // when & then
        assertThatThrownBy(() -> userCouponService.useCoupon(user2.getId(), userCoupon.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_COUPON_NOT_FOUND.getErrorMessage());
    }

    @DisplayName("유저가 자신이 보유한 모든 쿠폰들을 조회한다.")
    @Test
    void getUserCoupon() {
        // given
        User user = setupUser("123", "123", "123", null);
        Coupon coupon = setupCoupon("123", "123123123123", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000, CouponStatus.PREVIOUS);
        Coupon coupon2 = setupCoupon("1234", "1231231231234", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000, CouponStatus.PREVIOUS);
        Coupon coupon3 = setupCoupon("12345", "1231231231235", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000, CouponStatus.IN_PROGRESS);

        UserCoupon userCoupon = setupUserCoupon(user, coupon, EXPIRED);
        UserCoupon userCoupon2 = setupUserCoupon(user, coupon2, USED);
        UserCoupon userCoupon3 = setupUserCoupon(user, coupon3, UNUSED);

        userRepository.save(user);
        couponRepository.saveAll(List.of(coupon, coupon2, coupon3));
        userCouponRepository.saveAll(List.of(userCoupon, userCoupon2, userCoupon3));

        // when
        List<UserCouponGetResponse> responses = userCouponService.getUserCoupon(user.getId());

        // then
        assertThat(responses.getLast().id()).isEqualTo(userCoupon3.getId());
        assertThat(responses.getLast().content()).isEqualTo(userCoupon3.getCoupon().getContent());
        assertThat(responses.getFirst().id()).isEqualTo(userCoupon.getId());
        assertThat(responses.getFirst().content()).isEqualTo(userCoupon.getCoupon().getContent());
    }

    @DisplayName("유저가 커서 페이지 형식으로 자신이 보유한 모든 쿠폰들을 조회한다.")
    @Test
    void getUserCoupons() {
        // given
        User user = setupUser("123", "123", "123", null);
        Coupon coupon = setupCoupon("123", "123123123123", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000, CouponStatus.PREVIOUS);
        Coupon coupon2 = setupCoupon("1234", "1231231231234", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000, CouponStatus.PREVIOUS);
        Coupon coupon3 = setupCoupon("12345", "1231231231235", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000, CouponStatus.IN_PROGRESS);
        Coupon coupon4 = setupCoupon("12346", "1231231231236", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000, CouponStatus.ENDED);
        Coupon coupon5 = setupCoupon("12347", "1231231231237", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000, CouponStatus.COMPLETED);
        Coupon coupon6 = setupCoupon("12346", "1231231231238", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000, CouponStatus.IN_PROGRESS);
        Coupon coupon7 = setupCoupon("12347", "1231231231239", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000, CouponStatus.IN_PROGRESS);

        UserCoupon userCoupon = setupUserCoupon(user, coupon, EXPIRED);
        UserCoupon userCoupon2 = setupUserCoupon(user, coupon2, USED);
        UserCoupon userCoupon3 = setupUserCoupon(user, coupon3, UNUSED);
        UserCoupon userCoupon4 = setupUserCoupon(user, coupon4, USED);
        UserCoupon userCoupon5 = setupUserCoupon(user, coupon5, USED);
        UserCoupon userCoupon6 = setupUserCoupon(user, coupon6, UNUSED);
        UserCoupon userCoupon7 = setupUserCoupon(user, coupon7, EXPIRED);

        userRepository.save(user);
        couponRepository.saveAll(List.of(coupon, coupon2, coupon3, coupon4, coupon5, coupon6, coupon7));
        userCouponRepository.saveAll(List.of(userCoupon, userCoupon2, userCoupon3, userCoupon4, userCoupon5, userCoupon6, userCoupon7));

        // when
        UserCouponCursorResponse response = userCouponService.getUserCoupons(user.getId(), coupon2.getId(), 3);

        // then
        assertThat(response.total()).isEqualTo(7);
        assertThat(response.content().getFirst().content()).isEqualTo(coupon3.getContent());
        assertThat(response.content().getFirst().id()).isEqualTo(coupon3.getId());
        assertThat(response.content().getLast().content()).isEqualTo(coupon5.getContent());
        assertThat(response.content().getLast().id()).isEqualTo(coupon5.getId());

    }

    @DisplayName("특정 쿠폰 ID의 제목을 조회한다.")
    @Test
    void getCouponTitle() {
        // given
        Coupon coupon = setupCoupon("123", "123123123123", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000, CouponStatus.PREVIOUS);
        couponRepository.save(coupon);

        // when
        String response = userCouponService.getCouponTitle(coupon.getId());

        // then
        assertThat(response).isEqualTo(coupon.getTitle());
    }
}