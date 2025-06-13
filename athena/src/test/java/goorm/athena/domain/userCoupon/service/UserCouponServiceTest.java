package goorm.athena.domain.userCoupon.service;

import goorm.athena.domain.coupon.entity.Coupon;
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

import java.util.List;

import static goorm.athena.domain.userCoupon.entity.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserCouponServiceTest extends UserCouponIntegrationSupport {

    @DisplayName("유저가 진행 중인 쿠폰을 발급 요청하면 쿠폰 내용, 제목, 가격이 정상 반환된다.")
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

    @DisplayName("유저가 진행 중이 아닌 쿠폰을 발급 요청하면 INVALID_COUPON_STATUS 에러 메시지를 반환한다.")
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

    @DisplayName("유저가 이미 발급받은 쿠폰을 다시 발급 요청하면 ALREADY_ISSUED_COUPON 에러 메시지를 반환한다.")
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

    @DisplayName("유저가 발급받은 쿠폰을 사용하면 사용한 쿠폰의 상태가 USED로 변경된다.")
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

    @DisplayName("유저가 만료된(EXPIRED) 쿠폰을 사용 요청하면 INVALID_USE_COUPON 에러 메시지를 반환한다. (서비스에서 검증)")
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

    @DisplayName("유저가 만료된(EXPIRED) 쿠폰을 사용 요청하면 INVALID_USE_COUPON 에러 메시지를 반환한다. (도메인에서 검증)")
    @Test
    void useCoupon_INVALID_COUPON_STATUS() {
        // given
        User user = userRepository.findById(25L).get();
        Coupon coupon = couponRepository.findById(11L).get();
        UserCoupon userCoupon = setupUserCoupon(user, coupon, EXPIRED);

        // when & then
        assertThatThrownBy(userCoupon::useCoupon)
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INVALID_COUPON_STATUS.getErrorMessage());
    }

    @DisplayName("유저가 자신이 아닌 다른 유저의 쿠폰을 사용 요청하면 USER_COUPON_NOT_FOUND 에러 메시지를 반환한다.")
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

    @DisplayName("유저가 자신이 보유한 모든 쿠폰을 조회하면 쿠폰 목록의 첫 번째와 마지막 쿠폰이 올바르게 조회된다.")
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

    @DisplayName("유저가 커서 기반 페이지네이션으로 쿠폰 목록 조회 요청하면 요청한 범위 내 쿠폰 목록과 총 쿠폰 수가 정상 반환된다.")
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

    @DisplayName("유저가 보유한 특정 쿠폰 ID의 제목을 조회하면 해당 쿠폰의 제목을 정상 반환한다ㅏ.")
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