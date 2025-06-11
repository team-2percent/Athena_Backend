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

class UserCouponServiceTest extends UserCouponIntegrationSupport {

    @DisplayName("유저가 등록된 쿠폰들 중 '진행 중'인 쿠폰을 발급받는다.")
    @Test
    void issueCoupon_Success() {
        // given
        User user = userRepository.findById(25L).get();
        Coupon coupon = couponRepository.findById(11L).get();

        UserCouponIssueRequest request = new UserCouponIssueRequest(coupon.getId());

        // when
        UserCouponIssueResponse response = userCouponCommandService.issueCoupon(user.getId(), request);


        // then
        assertThat(response.content()).isEqualTo(coupon.getContent());
        assertThat(response.title()).isEqualTo(coupon.getTitle());
        assertThat(response.price()).isEqualTo(coupon.getPrice());
    }

    @DisplayName("유저가 '진행 중' 상태가 아닌 쿠폰을 발급받을려고 하면 에러를 리턴한다..")
    @Test
    void issueCoupon_NOT_INPROGRESS() {
        // given
        User user = userRepository.findById(25L).get();
        Coupon coupon = couponRepository.findById(7L).get();

        UserCouponIssueRequest request = new UserCouponIssueRequest(coupon.getId());

        // when & then
        assertThatThrownBy(() -> userCouponCommandService.issueCoupon(user.getId(), request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INVALID_COUPON_STATUS.getErrorMessage());
    }

    @DisplayName("유저가 이미 발급받은 쿠폰을 한번 더 발급받을려고 하면 에러를 리턴한다.")
    @Test
    void issueCoupon_ALREADY_ISSUED() {
        // given
        User user = userRepository.findById(25L).get();
        Coupon coupon = couponRepository.findById(11L).get();


        UserCouponIssueRequest request = new UserCouponIssueRequest(coupon.getId());

        userCouponCommandService.issueCoupon(user.getId(), request);

        // when & then
        assertThatThrownBy(() -> userCouponCommandService.issueCoupon(user.getId(), request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.ALREADY_ISSUED_COUPON.getErrorMessage());
    }

    @DisplayName("유저가 발급받은 쿠폰을 사용한다.")
    @Test
    void useCoupon_Success() {
        // given
        User user = userRepository.findById(25L).get();
        Coupon coupon = couponRepository.findById(11L).get();
        UserCoupon userCoupon = setupUserCoupon(user, coupon, UNUSED);

        userCouponRepository.save(userCoupon);

        // when
        userCouponCommandService.useCoupon(user.getId(), userCoupon.getId());

        // then
        assertThat(userCoupon.getStatus()).isEqualTo(Status.USED);
    }

    @DisplayName("유저가 사용 할 수 없는 쿠폰을 사용하려고 하면 에러를 리턴한다.")
    @Test
    void useCoupon_INVALID_USE_COUPON() {
        // given
        User user = userRepository.findById(25L).get();
        Coupon coupon = couponRepository.findById(11L).get();
        UserCoupon userCoupon = setupUserCoupon(user, coupon, EXPIRED);

        userCouponRepository.save(userCoupon);

        // when & then
        assertThatThrownBy(() -> userCouponCommandService.useCoupon(user.getId(), userCoupon.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INVALID_USE_COUPON.getErrorMessage());
    }

    @DisplayName("유저가 자신 이외의 쿠폰을 사용하려고 하면 에러를 리턴한다.")
    @Test
    void useCoupon_NOT_FOUND() {
        // given
        User user = userRepository.findById(25L).get();
        User user2 = userRepository.findById(26L).get();
        Coupon coupon = couponRepository.findById(11L).get();
        UserCoupon userCoupon = setupUserCoupon(user, coupon, UNUSED);

        userCouponRepository.save(userCoupon);

        // when & then
        assertThatThrownBy(() -> userCouponCommandService.useCoupon(user2.getId(), userCoupon.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_COUPON_NOT_FOUND.getErrorMessage());
    }

    @DisplayName("유저가 자신이 보유한 모든 쿠폰들을 조회한다.")
    @Test
    void getUserCoupon() {
        // given
        User user = userRepository.findById(24L).get();
        Coupon coupon = couponRepository.findById(12L).get();
        Coupon coupon2 = couponRepository.findById(13L).get();
        Coupon coupon3 = couponRepository.findById(14L).get();

        UserCoupon userCoupon = setupUserCoupon(user, coupon, EXPIRED);
        UserCoupon userCoupon2 = setupUserCoupon(user, coupon2, USED);
        UserCoupon userCoupon3 = setupUserCoupon(user, coupon3, UNUSED);

        userCouponRepository.saveAll(List.of(userCoupon, userCoupon2, userCoupon3));

        // when
        List<UserCouponGetResponse> responses = userCouponQueryService.getUserCoupon(user.getId());

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
        User user = userRepository.findById(25L).get();

        Coupon coupon = couponRepository.findById(11L).get();
        Coupon coupon2 = couponRepository.findById(12L).get();
        Coupon coupon3 = couponRepository.findById(13L).get();
        Coupon coupon4 = couponRepository.findById(14L).get();
        Coupon coupon5 = couponRepository.findById(15L).get();
        Coupon coupon6 = couponRepository.findById(16L).get();
        Coupon coupon7 = couponRepository.findById(17L).get();

        UserCoupon userCoupon = setupUserCoupon(user, coupon, EXPIRED);
        UserCoupon userCoupon2 = setupUserCoupon(user, coupon2, USED);
        UserCoupon userCoupon3 = setupUserCoupon(user, coupon3, UNUSED);
        UserCoupon userCoupon4 = setupUserCoupon(user, coupon4, USED);
        UserCoupon userCoupon5 = setupUserCoupon(user, coupon5, USED);
        UserCoupon userCoupon6 = setupUserCoupon(user, coupon6, UNUSED);
        UserCoupon userCoupon7 = setupUserCoupon(user, coupon7, EXPIRED);

        userCouponRepository.saveAll(List.of(userCoupon, userCoupon2, userCoupon3, userCoupon4, userCoupon5, userCoupon6, userCoupon7));

        // when
        UserCouponCursorResponse response = userCouponQueryService.getUserCoupons(user.getId(), userCoupon2.getId(), 3);

        // then
        assertThat(response.total()).isEqualTo(7);
        assertThat(response.content().getFirst().content()).isEqualTo(coupon3.getContent());
        assertThat(response.content().getFirst().id()).isEqualTo(userCoupon3.getId());
        assertThat(response.content().getLast().content()).isEqualTo(coupon5.getContent());
        assertThat(response.content().getLast().id()).isEqualTo(userCoupon5.getId());

    }

    @DisplayName("특정 쿠폰 ID의 제목을 조회한다.")
    @Test
    void getCouponTitle() {
        // given
        Coupon coupon = couponRepository.findById(21L).get();

        // when
        String response = userCouponQueryService.getCouponTitle(coupon.getId());

        // then
        assertThat(response).isEqualTo(coupon.getTitle());
    }
}