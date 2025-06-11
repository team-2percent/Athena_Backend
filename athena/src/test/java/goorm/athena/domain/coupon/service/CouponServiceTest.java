package goorm.athena.domain.coupon.service;

import goorm.athena.domain.coupon.dto.req.CouponCreateRequest;
import goorm.athena.domain.coupon.dto.res.CouponCreateResponse;
import goorm.athena.domain.coupon.dto.res.CouponEventGetResponse;
import goorm.athena.domain.coupon.dto.res.CouponGetDetailResponse;
import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.coupon.util.CouponIntegrationTestSupport;
import goorm.athena.domain.userCoupon.dto.req.UserCouponIssueRequest;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponServiceTest extends CouponIntegrationTestSupport {

    @DisplayName("입력받은 값으로 쿠폰을 생성한다.")
    @Test
    void createCoupon() {
        // given
        CouponCreateRequest request = new CouponCreateRequest("123", "121231231233", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000);

        // when
        CouponCreateResponse response = couponCommandService.createCoupon(request);

        // then
        assertThat(response.content()).isEqualTo("121231231233");
        assertThat(response.title()).isEqualTo("123");
    }

    @DisplayName("관리자 유저가 페이지 형식으로 등록된 모든 쿠폰들을 조회한다.")
    @Test
    void getCoupons() {
        // given

        // when
        Page<Coupon> coupons = couponQueryService.getCoupons(0, 10);

        // then
        assertThat(coupons.getContent()).hasSize(10);
        assertThat(coupons.getTotalElements()).isEqualTo(30);
        assertThat(coupons.getTotalPages()).isEqualTo(3);
        assertThat(coupons.getNumber()).isEqualTo(0); // 현재 페이지 번호
        assertThat(coupons.getSize()).isEqualTo(10);  // 요청한 사이즈
    }

    @DisplayName("관리자 유저가 페이지 형식으로 등록된 'PREVIOUS' = 발급 이전 타입의 쿠폰들을 조회한다.")
    @Test
    void getCouponByStatus_PREVIOUS() {
        // given

        // when
        Page<Coupon> coupons = couponQueryService.getCouponByStatus(0, 10, CouponStatus.PREVIOUS);

        // then
        assertThat(coupons.getContent()).hasSize(10);
        assertThat(coupons.getTotalElements()).isEqualTo(10);
        assertThat(coupons.getTotalPages()).isEqualTo(1);
        assertThat(coupons.getNumber()).isEqualTo(0); // 현재 페이지 번호
        assertThat(coupons.getSize()).isEqualTo(10);  // 요청한 사이즈
    }

    @DisplayName("관리자 유저가 페이지 형식으로 등록된 'IN_PROGRESS' = 발급 중 타입의 쿠폰들을 조회한다.")
    @Test
    void getCouponByStatus_IN_PROGRESS() {
        // given

        // when
        Page<Coupon> coupons = couponQueryService.getCouponByStatus(0, 10, CouponStatus.IN_PROGRESS);

        // then
        assertThat(coupons.getContent()).hasSize(10);
        assertThat(coupons.getTotalElements()).isEqualTo(10);
        assertThat(coupons.getTotalPages()).isEqualTo(1);
        assertThat(coupons.getNumber()).isEqualTo(0); // 현재 페이지 번호
        assertThat(coupons.getSize()).isEqualTo(10);  // 요청한 사이즈
    }

    @DisplayName("관리자 유저가 페이지 형식으로 등록된 'COMPLETED' = 발급 완료 타입의 쿠폰들을 조회한다.")
    @Test
    void getCouponByStatus_COMPLETED() {
        // given

        // when
        Page<Coupon> coupons = couponQueryService.getCouponByStatus(0, 10, CouponStatus.COMPLETED);

        // then
        assertThat(coupons.getContent()).hasSize(5);
        assertThat(coupons.getTotalElements()).isEqualTo(5);
        assertThat(coupons.getTotalPages()).isEqualTo(1);
        assertThat(coupons.getNumber()).isEqualTo(0); // 현재 페이지 번호
        assertThat(coupons.getSize()).isEqualTo(10);  // 요청한 사이즈
    }

    @DisplayName("관리자 유저가 페이지 형식으로 등록된 'ENDED' = 발급 종료 타입의 쿠폰들을 조회한다.")
    @Test
    void getCouponByStatus_ENDED() {
        // given

        // when
        Page<Coupon> coupons = couponQueryService.getCouponByStatus(0, 10, CouponStatus.ENDED);

        // then
        assertThat(coupons.getContent()).hasSize(5);
        assertThat(coupons.getTotalElements()).isEqualTo(5);
        assertThat(coupons.getTotalPages()).isEqualTo(1);
        assertThat(coupons.getNumber()).isEqualTo(0); // 현재 페이지 번호
        assertThat(coupons.getSize()).isEqualTo(10);  // 요청한 사이즈
    }

    @DisplayName("관리자 유저가 특정 쿠폰의 상세 내용을 조회한다.")
    @Test
    void getCouponDetail() {
        // given
        Coupon coupon = couponRepository.findById(25L).get();

        // when
        CouponGetDetailResponse response = couponQueryService.getCouponDetail(coupon.getId());

        // then
        assertThat(response.id()).isEqualTo(coupon.getId());
        assertThat(response.title()).isEqualTo(coupon.getTitle());
        assertThat(response.content()).isEqualTo(coupon.getContent());
        assertThat(response.status()).isEqualTo(coupon.getCouponStatus());
    }

    @DisplayName("특정 Id의 쿠폰의 내용들을 조회한다. ( 도메인 조회 용 ) ")
    @Test
    void getCoupon_SUCCESS() {
        // given
        Coupon coupon = couponRepository.findById(25L).get();

        // when
        Coupon response = couponQueryService.getCoupon(25L);

        // then
        assertThat(response.getId()).isEqualTo(coupon.getId());
        assertThat(response.getTitle()).isEqualTo(coupon.getTitle());
        assertThat(response.getContent()).isEqualTo(coupon.getContent());
        assertThat(response.getCouponStatus()).isEqualTo(coupon.getCouponStatus());
    }

    @DisplayName("존재하지 않는 쿠폰 ID를 조회하면 에러를 리턴한다.")
    @Test
    void getCoupon_Error() {
        // given

        // when & then
        assertThatThrownBy(() -> couponQueryService.getCoupon(1000000L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.COUPON_NOT_FOUND.getErrorMessage());
    }

    @DisplayName("유저가 현재 발급 중인 쿠폰 이벤트들을 조회한다. ( 미사용 ) ")
    @Test
    void getCouponEvent_UNUSED() {
        // given
        Long userId = 20L;
        UserCouponIssueRequest request = new UserCouponIssueRequest(19L);

        userCouponCommandService.issueCoupon(userId, request);

        // when
        List<CouponEventGetResponse> responses = couponQueryService.getCouponEvent(userId);

        // then
        assertThat(responses.size()).isEqualTo(10);
        assertThat(responses.getFirst().userIssued()).isFalse();
        assertThat(responses.getLast().userIssued()).isFalse();
    }

    @DisplayName("유저가 현재 발급 중인 쿠폰 이벤트들을 조회한다. ( 사용 ) ")
    @Test
    void getCouponEvent_USED() {
        // given
        Long userId = 20L;
        UserCouponIssueRequest request = new UserCouponIssueRequest(16L);

        userCouponCommandService.issueCoupon(userId, request);

        // when
        List<CouponEventGetResponse> responses = couponQueryService.getCouponEvent(userId);

        // then
        assertThat(responses.size()).isEqualTo(10);
        assertThat(responses.get(4).userIssued()).isTrue();
        assertThat(responses.getLast().userIssued()).isFalse();
    }
}