package goorm.athena.domain.coupon.service;

import goorm.athena.domain.coupon.dto.req.CouponCreateRequest;
import goorm.athena.domain.coupon.dto.res.CouponCreateResponse;
import goorm.athena.domain.coupon.dto.res.CouponEventGetResponse;
import goorm.athena.domain.coupon.dto.res.CouponGetDetailResponse;
import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.coupon.util.CouponIntegrationTestSupport;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
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

    @DisplayName("관리자가 쿠폰을 생성하면, 입력한 내용과 생성된 정보가 일치한다.")
    @Test
    void createCoupon() {
        // given
        User user = userRepository.findById(1L).get();
        CouponCreateRequest request = new CouponCreateRequest("123", "121231231233", 10000, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(50), 1000);

        // when
        CouponCreateResponse response = couponCommandService.createCoupon(request);

        // then
        assertThat(user.getRole()).isEqualTo(Role.ROLE_ADMIN); // 관리자 유저가 맞는지 검증
        assertThat(response.content()).isEqualTo("121231231233");
        assertThat(response.title()).isEqualTo("123");
        assertThat(response.price()).isEqualTo(10000);
        assertThat(response.stock()).isEqualTo(1000);
    }

    @DisplayName("관리자가 등록된 첫 번째 페이지(사이즈: 10) 쿠폰들을 조회하면, 정상적으로 페이지네이션된 쿠폰 목록을 반환한다.")
    @Test
    void getCoupons() {
        // given
        User user = userRepository.findById(1L).get();

        // when
        Page<Coupon> coupons = couponQueryService.getCoupons(0, 10);

        // then
        assertThat(user.getRole()).isEqualTo(Role.ROLE_ADMIN); // 관리자 유저가 맞는지 검증
        assertThat(coupons.getContent()).hasSize(10); // 현재 페이지에서 조회한 사이즈
        assertThat(coupons.getTotalElements()).isEqualTo(30); // 조회된 내용의 모든 데이터 사이즈
        assertThat(coupons.getTotalPages()).isEqualTo(3); // 조회된 내용의 모든 페이지 번호
        assertThat(coupons.getNumber()).isEqualTo(0); // 현재 페이지 번호
        assertThat(coupons.getSize()).isEqualTo(10);  // 요청한 사이즈
    }

    @DisplayName("관리자가 'PREVIOUS' 상태 쿠폰을 첫 페이지(사이즈: 10)로 조회하면, 발급 이전 쿠폰 목록을 반환한다.")
    @Test
    void getCouponByStatus_PREVIOUS() {
        // given
        User user = userRepository.findById(1L).get();

        // when
        Page<Coupon> coupons = couponQueryService.getCouponByStatus(0, 10, CouponStatus.PREVIOUS);

        // then
        assertThat(user.getRole()).isEqualTo(Role.ROLE_ADMIN); // 관리자 유저가 맞는지 검증
        assertThat(coupons.getContent()).hasSize(10); // 현재 페이지에서 조회한 사이즈
        assertThat(coupons.getTotalElements()).isEqualTo(10); // 조회된 내용의 모든 데이터 사이즈
        assertThat(coupons.getTotalPages()).isEqualTo(1);  // 조회된 내용의 모든 페이지 번호
        assertThat(coupons.getNumber()).isEqualTo(0); // 현재 페이지 번호
        assertThat(coupons.getSize()).isEqualTo(10);  // 요청한 사이즈
    }

    @DisplayName("관리자가 'IN_PROGRESS' = 발급 중 상태 쿠폰을 첫 페이지(사이즈: 10)로 조회하면, 발급 중인 쿠폰 목록을 반환한다.")
    @Test
    void getCouponByStatus_IN_PROGRESS() {
        // given
        User user = userRepository.findById(1L).get();

        // when
        Page<Coupon> coupons = couponQueryService.getCouponByStatus(0, 10, CouponStatus.IN_PROGRESS);

        // then
        assertThat(user.getRole()).isEqualTo(Role.ROLE_ADMIN); // 관리자 유저가 맞는지 검증
        assertThat(coupons.getContent()).hasSize(10); // 관리자 유저가 맞는지 검증
        assertThat(coupons.getTotalElements()).isEqualTo(10); // 조회된 내용의 모든 데이터 사이즈
        assertThat(coupons.getTotalPages()).isEqualTo(1);  // 조회된 내용의 모든 페이지 번호
        assertThat(coupons.getNumber()).isEqualTo(0); // 현재 페이지 번호
        assertThat(coupons.getSize()).isEqualTo(10);  // 요청한 사이즈
    }

    @DisplayName("관리자가 'COMPLETED' = 발급 완료 상태 쿠폰을 첫 페이지(사이즈: 10)로 조회하면, 발급 완료된 쿠폰 목록을 반환한다.")
    @Test
    void getCouponByStatus_COMPLETED() {
        // given
        User user = userRepository.findById(1L).get();

        // when
        Page<Coupon> coupons = couponQueryService.getCouponByStatus(0, 10, CouponStatus.COMPLETED);

        // then
        assertThat(user.getRole()).isEqualTo(Role.ROLE_ADMIN); // 관리자 유저가 맞는지 검증
        assertThat(coupons.getContent()).hasSize(5); // 관리자 유저가 맞는지 검증
        assertThat(coupons.getTotalElements()).isEqualTo(5); // 조회된 내용의 모든 데이터 사이즈
        assertThat(coupons.getTotalPages()).isEqualTo(1);  // 조회된 내용의 모든 페이지 번호
        assertThat(coupons.getNumber()).isEqualTo(0); // 현재 페이지 번호
        assertThat(coupons.getSize()).isEqualTo(10);  // 요청한 사이즈
    }

    @DisplayName("관리자가 'ENDED' = 발급 종료 상태 쿠폰을 첫 페이지(사이즈: 10)로 조회하면, 발급 종료된 쿠폰 목록을 반환한다.")
    @Test
    void getCouponByStatus_ENDED() {
        // given
        User user = userRepository.findById(1L).get();

        // when
        Page<Coupon> coupons = couponQueryService.getCouponByStatus(0, 10, CouponStatus.ENDED);

        // then
        assertThat(user.getRole()).isEqualTo(Role.ROLE_ADMIN); // 관리자 유저가 맞는지 검증
        assertThat(coupons.getContent()).hasSize(5); // 관리자 유저가 맞는지 검증
        assertThat(coupons.getTotalElements()).isEqualTo(5); // 조회된 내용의 모든 데이터 사이즈
        assertThat(coupons.getTotalPages()).isEqualTo(1);  // 조회된 내용의 모든 페이지 번호
        assertThat(coupons.getNumber()).isEqualTo(0); // 현재 페이지 번호
        assertThat(coupons.getSize()).isEqualTo(10);  // 요청한 사이즈
    }

    @DisplayName("관리자가 특정 쿠폰의 상세 정보를 조회하면, 해당 쿠폰의 제목, 내용, 상태를 반환한다.")
    @Test
    void getCouponDetail() {
        // given
        User user = userRepository.findById(1L).get();
        Coupon coupon = couponRepository.findById(1L).get();

        // when
        CouponGetDetailResponse response = couponQueryService.getCouponDetail(coupon.getId());

        // then
        assertThat(user.getRole()).isEqualTo(Role.ROLE_ADMIN); // 관리자 유저가 맞는지 검증
        assertThat(response.id()).isEqualTo(coupon.getId());
        assertThat(response.title()).isEqualTo(coupon.getTitle());
        assertThat(response.content()).isEqualTo(coupon.getContent());
        assertThat(response.status()).isEqualTo(coupon.getCouponStatus());
    }

    @DisplayName("관리자가 특정 ID의 쿠폰을 조회하면, 해당 쿠폰의 엔티티 정보(id, 제목, 내용, 상태)를 반환한다.")
    @Test
    void getCoupon_SUCCESS() {
        // given
        User user = userRepository.findById(1L).get();
        Coupon coupon = couponRepository.findById(25L).get();

        // when
        Coupon response = couponQueryService.getCoupon(25L);

        // then
        assertThat(user.getRole()).isEqualTo(Role.ROLE_ADMIN); // 관리자 유저가 맞는지 검증
        assertThat(response.getId()).isEqualTo(coupon.getId());
        assertThat(response.getTitle()).isEqualTo(coupon.getTitle());
        assertThat(response.getContent()).isEqualTo(coupon.getContent());
        assertThat(response.getCouponStatus()).isEqualTo(coupon.getCouponStatus());
    }

    @DisplayName("관리자가 존재하지 않는 쿠폰 ID를 조회하면, 'COUPON_NOT_FOUND' 에러를 반환한다.")
    @Test
    void getCoupon_Error() {
        // given
        User user = userRepository.findById(1L).get();

        // when & then
        assertThat(user.getRole()).isEqualTo(Role.ROLE_ADMIN); // 관리자 유저가 맞는지 검증
        assertThatThrownBy(() -> couponQueryService.getCoupon(1000000L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.COUPON_NOT_FOUND.getErrorMessage());
    }

    @DisplayName("로그인한 20번 유저가 , 현재 발급 가능한 쿠폰 이벤트 목록을 조회하면 " +
            "사용하지 않은 쿠폰들은 '미사용 상태(userIssued=false)'로 조회된다.")
    @Test
    void getCouponEvent_UNUSED() {
        // given
        Long userId = 20L;

        // when
        List<CouponEventGetResponse> responses = couponQueryService.getCouponEvent(userId);

        // then
        assertThat(responses.size()).isEqualTo(10);

        assertThat(responses.get(4).userIssued()).isFalse();
        assertThat(responses.getLast().userIssued()).isFalse();
    }

    @DisplayName("로그인한 20번 유저가 16번 쿠폰을 발급받은 뒤, 현재 발급 가능한 쿠폰 이벤트 목록을 조회하면 " +
            "해당 쿠폰이 '사용 상태(userIssued=true)'로 조회된다.")
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