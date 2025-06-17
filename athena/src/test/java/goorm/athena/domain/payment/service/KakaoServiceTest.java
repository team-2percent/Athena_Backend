package goorm.athena.domain.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import goorm.athena.domain.payment.PaymentIntergrationTestSupport;
import goorm.athena.domain.payment.dto.req.PaymentApproveRequest;
import goorm.athena.domain.payment.dto.req.PaymentReadyRequest;
import goorm.athena.domain.payment.dto.res.KakaoPayApproveResponse;
import goorm.athena.domain.payment.dto.res.KakaoPayReadyResponse;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class KakaoServiceTest {

    @InjectMocks
    private KakaoPayService kakaoPayService;

    @Mock
    private RestTemplate restTemplate;

    // 카카오 페이 요청용 json을 담는 용도
    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName(" 카카오페이에 결제 요청에서 예외 발생 시 '카카오페이 결제 요청 실패' 발생" +
            "(결제 창 생성 요청 실패 상황)")
    void requestKakaoPayment_restTemplateThrows_thenThrowCustomException() throws Exception {
        // given
        PaymentReadyRequest requestDto = new PaymentReadyRequest("프로젝트", 1, 10000L, "홍길동");
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        Long orderId = 123L;

        given(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(KakaoPayReadyResponse.class))
        ).willThrow(new RuntimeException("카카오 서버 오류"));

        // when & then
        assertThatThrownBy(() -> kakaoPayService.requestKakaoPayment(requestDto, user, orderId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.KAKAO_PAY_REQUEST_FAILED.getErrorMessage());
    }

    @Test
    @DisplayName("결제 승인 요청중 카카오 서버 오류 발생시 '카카오페이 승인 실패' 발생")
    void approveKakaoPayment_restTemplateThrows_thenThrowCustomException() {
        // given
        PaymentApproveRequest requestDto = new PaymentApproveRequest(123L, "PG_TOKEN");
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);

        given(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(KakaoPayApproveResponse.class))
        ).willThrow(new RuntimeException("카카오 서버 오류"));

        // when & then
        assertThatThrownBy(() -> kakaoPayService.approveKakaoPayment("TID123", requestDto, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.KAKAO_PAY_APPROVE_FAILED.getErrorMessage());
    }
}
