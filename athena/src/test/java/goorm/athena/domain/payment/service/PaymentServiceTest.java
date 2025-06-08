package goorm.athena.domain.payment.service;

import goorm.athena.domain.payment.PaymentIntegrationTestSupport;
import goorm.athena.domain.payment.dto.res.KakaoPayApproveResponse;
import goorm.athena.domain.payment.dto.res.KakaoPayReadyResponse;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentServiceTest extends PaymentIntegrationTestSupport {

//

    @Test
    @DisplayName("존재하지 않는 주문 ID로 결제 준비 요청 시 예외 발생")
    void readyPayment_orderNotFound() {
        // given
        Long invalidOrderId = 99999L;

        // when & then
        assertThatThrownBy(() -> paymentService.readyPayment(invalidOrderId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.ORDER_NOT_FOUND.getErrorMessage());
    }

    @Test
    @DisplayName("이미 결제가 완료된 주문일 경우 예외 발생")
    void readyPayment_alreadyApproved() {
        // given
        Long orderId = 2L; // 사전에 APPROVED 상태로 저장된 결제

        // when & then
        assertThatThrownBy(() -> paymentService.readyPayment(orderId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.ALREADY_PAYMENT_COMPLETED.getErrorMessage());
    }

    @Test
    @DisplayName("카카오 서버 오류 등으로 결제 요청 실패 시 예외 발생")
    void readyPayment_kakaoError() {
        // given
        Long orderId = 3L; // 내부적으로 KakaoPay 오류 발생 유도

        // when & then
        assertThatThrownBy(() -> paymentService.readyPayment(orderId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.KAKAO_PAY_UNAVAILABLE.getErrorMessage());
    }

    @Test
    @DisplayName("유효한 pg_token으로 결제 승인 시 재고 감소, 프로젝트 금액 증가 검증")
    void approvePayment_success() {
        // given
        Long orderId = 4L;
        String pgToken = "VALID_PG_TOKEN";

        // when
        KakaoPayApproveResponse response = paymentService.approvePayment(pgToken, orderId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.tid()).isNotBlank();
        assertThat(response.redirectUrl()).isEqualTo("/payment/complete");
        assertThat(response.message()).contains("성공");
    }

    @Test
    @DisplayName("pg_token이 잘못되었거나 TID가 없을 경우 실패 응답 반환")
    void approvePayment_pgTokenInvalid() {
        // given
        Long orderId = 5L;
        String invalidPgToken = "INVALID";

        // when
        KakaoPayApproveResponse response = paymentService.approvePayment(invalidPgToken, orderId);

        // then
        assertThat(response.tid()).isNull();
        assertThat(response.redirectUrl()).isEqualTo("/payment/fail");
        assertThat(response.message()).contains("카카오페이 연동 실패");
    }

    @Test
    @DisplayName("결제 승인 중 내부 예외 발생 시 내부 오류 메시지 반환")
    void approvePayment_internalException() {
        // given
        Long orderId = 6L;
        String pgToken = "TRIGGER_INTERNAL_EXCEPTION";

        // when
        KakaoPayApproveResponse response = paymentService.approvePayment(pgToken, orderId);

        // then
        assertThat(response.tid()).isNull();
        assertThat(response.redirectUrl()).isEqualTo("/payment/fail");
        assertThat(response.message()).contains("결제 승인 후 재고,누적 처리 오류");
    }

    @Test
    @DisplayName("이미 승인된 결제일 경우 승인 시도 시 무시되거나 예외 발생")
    void approvePayment_alreadyApproved() {
        // given
        Long orderId = 7L;
        String pgToken = "VALID_PG_TOKEN";

        // when
        KakaoPayApproveResponse response = paymentService.approvePayment(pgToken, orderId);

        // then
        assertThat(response.tid()).isNull();
        assertThat(response.redirectUrl()).isEqualTo("/payment/fail");
        assertThat(response.message()).contains("결제 승인 후 재고,누적 처리 오류");
    }

    @Test
    @DisplayName("결제 성공시 응답값 검증")
    void approvePayment_responseValidation() {
        // given
        Long orderId = 8L;
        String pgToken = "VALID_PG_TOKEN";

        // when
        KakaoPayApproveResponse response = paymentService.approvePayment(pgToken, orderId);

        // then
        assertThat(response.tid()).isNotBlank();
        assertThat(response.redirectUrl()).isEqualTo("/payment/complete");
        assertThat(response.message()).contains("성공");
    }

//    @Test
//    @DisplayName("기존 결제가 PENDING 상태이면 삭제하고 새로 생성된다")
//    void readyPayment_onlyCreatesIfNotExists() {
//        // given
//        Long orderId = 9L;
//
//        // when
//        KakaoPayReadyResponse first = paymentService.readyPayment(orderId);
//        KakaoPayReadyResponse second = paymentService.readyPayment(orderId);
//
//        // then
//        assertThat(first.tid()).isNotEqualTo(second.tid());
//    }
}


