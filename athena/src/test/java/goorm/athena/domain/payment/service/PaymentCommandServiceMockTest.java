package goorm.athena.domain.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.order.service.OrderCommendService;
import goorm.athena.domain.order.service.OrderQueryService;
import goorm.athena.domain.orderitem.entity.OrderItem;
import goorm.athena.domain.orderitem.repository.OrderItemRepository;
import goorm.athena.domain.payment.dto.req.PaymentApproveRequest;
import goorm.athena.domain.payment.dto.res.Amount;
import goorm.athena.domain.payment.dto.res.KakaoPayApproveResponse;
import goorm.athena.domain.payment.dto.res.KakaoPayReadyResponse;
import goorm.athena.domain.payment.entity.Payment;
import goorm.athena.domain.payment.entity.Status;
import goorm.athena.domain.payment.repository.PaymentRepository;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.project.entity.Project;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PaymentCommandServiceMockTest {
    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private KakaoPayService kakaoPayService;

    @Mock
    private OrderCommendService orderCommendService;

    @InjectMocks
    private PaymentCommandService paymentCommandService;

    @Mock
    protected OrderItemRepository orderItemRepository;

    @Mock
    private OrderQueryService orderQueryService;

    @Mock
    private RestTemplate restTemplate;


    @Test
    @DisplayName("(결제 승인 후 재고 감소/누적 금액 증가) 메서드 처리 중 예외 발생 시 실패 응답이 반환된다" +
            "(재고가 0인 상황에서 결제가 진행되는 경우 에러가 발생 해야한다)")
    void approvePayment_internalPostProcessFail_thenReturnFailureResponse() {
        // given
        Long orderId = 4L;
        String pgToken = "PG_TOKEN_INTERNAL_ERROR";

        // 결제 임시 객체 생성
        Order order = new Order();
        User user = new User();
        Payment payment = Payment.create(order, user, "TID_TEST", 10000L);

        // 강제로 예외를 위해 재고 0으로 설정
        Product product = new Product();
        ReflectionTestUtils.setField(product, "stock", 0L);

        OrderItem orderItem = new OrderItem();
        ReflectionTestUtils.setField(orderItem, "product", product);

        given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.of(payment));
        given(orderItemRepository.findByOrderId(orderId)).willReturn(List.of(orderItem));

        KakaoPayApproveResponse fakeResponse = KakaoPayApproveResponse.ofSuccess(new KakaoPayApproveResponse(
                "aid", payment.getTid(), "cid", "CARD",
                new Amount(10000, 0, 0, 0, 0),
                "상품", "ITEM001", 1, LocalDateTime.now(), null, null
        ));

        given(kakaoPayService.approveKakaoPayment(eq(payment.getTid()), any(PaymentApproveRequest.class), eq(user))
        ).willReturn(fakeResponse);

        doThrow(new RuntimeException("재고 처리 실패"))
                .when(orderCommendService).postPaymentProcess(orderId);

        // when
        KakaoPayApproveResponse response = paymentCommandService.approvePayment(pgToken, orderId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.redirectUrl()).isEqualTo("/payment/fail");
        assertThat(response.message()).isEqualTo("결제 승인 후 재고,누적 처리 오류");
    }

    @Test
    @DisplayName("결제 승인 요청이 성공하면 상태가 APPROVED로 바뀌고 후속 처리( postPaymentProcess()= 재고 감소,후원 결제 금액 추가)가 정상 수행된다" +
            "(정상 결제)")
    void approvePayment_success_thenApprovedAndProcessed() {
        // given
        Long orderId = 5L;
        String pgToken = "PG_TOKEN_SUCCESS";

        User user = new User();
        Order order = new Order();
        Payment payment = Payment.create(order, user, "TID_TEST", 10000L);

        given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.of(payment));

        KakaoPayApproveResponse fakeResponse = KakaoPayApproveResponse.ofSuccess(
                new KakaoPayApproveResponse("aid", payment.getTid(), "cid", "CARD",
                        new Amount(10000, 0, 0, 0, 0),
                        "테스트 상품", "ITEM001", 1, LocalDateTime.now(), null, null)
        );

        given(kakaoPayService.approveKakaoPayment(any(), any(), any())).willReturn(fakeResponse);
        // when
        KakaoPayApproveResponse response = paymentCommandService.approvePayment(pgToken, orderId);

        // then
        assertThat(response).isNotNull();
        assertThat(payment.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(payment.getPgToken()).isEqualTo(pgToken);
        verify(orderCommendService).postPaymentProcess(orderId);
    }

    @Test
    @DisplayName("카카오페이 결제 요청 중 서버 장애 발생 시 '카카오페이 서버와의 통신에 실패했습니다' 예외가 발생한다")
    void readyPayment_kakaoPayRequestFails_thenThrowCustomException() {
        // given
        Long orderId = 2L;

        Order fakeOrder = new Order();
        User fakeUser = new User();
        Project fakeProject = new Project();
        ReflectionTestUtils.setField(fakeOrder, "user", fakeUser);
        ReflectionTestUtils.setField(fakeProject, "title", "테스트 프로젝트");
        ReflectionTestUtils.setField(fakeOrder, "project", fakeProject);

        given(orderQueryService.getById(orderId)).willReturn(fakeOrder);

        given(kakaoPayService.requestKakaoPayment(any(), any(), any()))
                .willThrow(new RuntimeException("카카오 서버 장애"));

        // when & then
        assertThatThrownBy(() -> paymentCommandService.readyPayment(orderId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.KAKAO_PAY_UNAVAILABLE.getErrorMessage());
    }


    @Test
    @DisplayName("createHttpEntity 중 JSON 직렬화 오류 발생 시 'JSON 직렬화 실패' 발생")
    void createHttpEntity_jsonProcessingError_thenThrowCustomException() throws JsonProcessingException {
        // given
        Map<String, Object> params = new HashMap<>();
        params.put("cid", "TEST_CID");

        ObjectMapper mockMapper = mock(ObjectMapper.class);
        KakaoPayService brokenService = new KakaoPayService(restTemplate, mockMapper);
        ReflectionTestUtils.setField(brokenService, "adminKey", "test-key");

        when(mockMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("직렬화 실패") {});

        // when & then
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(brokenService, "createHttpEntity", params))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.JSON_PROCESSING_ERROR.getErrorMessage());
    }

    @Test
    @DisplayName("카카오페이 승인 API 호출 실패 시 '카카오페이 연동 실패' 응답이 반환된다")
    void approvePayment_kakaoApiFail_thenReturnFailureResponse() {
        // given
        Long orderId = 4L;
        String pgToken = "PG_TOKEN_FAIL";

        Order order = new Order();
        User user = new User();
        Payment payment = Payment.create(order, user, "TID_TEST", 10000L);

        given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.of(payment));
        given(kakaoPayService.approveKakaoPayment(eq(payment.getTid()), any(), eq(user)))
                .willThrow(new CustomException(ErrorCode.KAKAO_PAY_APPROVE_FAILED));


        // when
        KakaoPayApproveResponse response = paymentCommandService.approvePayment(pgToken, orderId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.tid()).isNull();
        assertThat(response.message()).contains("카카오페이 연동 실패");
        verify(kakaoPayService).approveKakaoPayment(eq(payment.getTid()), any(), any(User.class));
    }

    @Test
    @DisplayName("결제 정보가 존재하지 않는 결제 ID 대해 결제 호출 시 '해당 ID의 결제가 존재하지 않습니다' 예외가 발생한다")
    void approvePayment_paymentNotFound_thenThrow() {
        // given
        Long invalidOrderId = 9999L;


        // when & then
        assertThatThrownBy(() -> paymentCommandService.approvePayment("PG_TOKEN", invalidOrderId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.PAYMENT_NOT_FOUND.getErrorMessage());
    }

    @Test
    @DisplayName("승인된 결제(상태: APPROVED)가 이미 존재하는 주문에 대해 결제 요청 시 예외가 발생한다" +
            "(결제 중복 방지 로직이 정상적으로 동작하는지 확인하는 예외)")
    void readyPayment_approvedPaymentExists_thenThrow() {
        // given
        Long orderId = 1L;

        User user = new User();
        Project project = new Project();
        Order order = new Order();

        // 먼저 order 안의 필드를 세팅 (순서 중요)
        ReflectionTestUtils.setField(order, "user", user);
        ReflectionTestUtils.setField(order, "project", project);
        ReflectionTestUtils.setField(project, "title", "테스트 프로젝트");

        // Payment 생성 (order와 user가 모두 세팅된 상태여야 함)
        Payment payment = Payment.create(order, user, "TID_APPROVED", 10000L);

        // 상태만 APPROVED로 바꿔주고
        ReflectionTestUtils.setField(payment, "status", Status.APPROVED);

        // 실제 payment 내부의 order 확인 로그
        System.out.println("payment.getOrder() = " + payment.getOrder());
        System.out.println("payment.getOrder().getUser() = " + payment.getOrder().getUser());

        // mocking 설정
        given(orderQueryService.getById(orderId)).willReturn(order);
        given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.of(payment));

        // when & then
        assertThatThrownBy(() -> paymentCommandService.readyPayment(orderId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.ALREADY_PAYMENT_COMPLETED.getErrorMessage());
    }

    @Test
    @DisplayName("결제 이력이 없는 주문 ID에 대해 readyPayment 호출 시 결제가 저장되고 응답 결과 TID가 일치한다" +
            "(정상적인 결제)")
    void readyPayment_success() {
        // given
        Long orderId = 4L;
        Order order = new Order();
        User user = new User();

        ReflectionTestUtils.setField(order, "user", user);
        Project project = new Project();
        ReflectionTestUtils.setField(order, "project", project);

        given(orderQueryService.getById(orderId)).willReturn(order);
        given(kakaoPayService.requestKakaoPayment(any(), any(), any()))
                .willReturn(new KakaoPayReadyResponse("https://mock.url", "TID_MOCK_123"));

        // when
        KakaoPayReadyResponse response = paymentCommandService.readyPayment(orderId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.tid()).isEqualTo("TID_MOCK_123");
    }

    @Test
    @DisplayName("PENDING 상태의 결제가 있는 주문에 대해" +
            " 다시 readyPayment 호출 시 기존 결제가 삭제되고 새 결제가 저장된다" +
            "(사용자가 결제를 시도하다가 중단하고 다시 결제 시도하는 경우)")
    void readyPayment_existingPendingPaymentDeleted_withDataSql() {
        // given
        Long orderId = 2L;
        Order order = new Order();
        User user = new User();

        Project project = new Project();
        ReflectionTestUtils.setField(project, "title", "테스트 프로젝트");
        ReflectionTestUtils.setField(order, "project", project);
        ReflectionTestUtils.setField(order, "user", user);

        Payment oldPayment = Payment.create(order, user, "TID_OLD", 10000L);
        ReflectionTestUtils.setField(oldPayment, "id", 10L);

        given(orderQueryService.getById(orderId)).willReturn(order);
        given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.of(oldPayment));
        given(kakaoPayService.requestKakaoPayment(any(), any(), any()))
                .willReturn(new KakaoPayReadyResponse("https://redirect.success.com", "TID_NEW_0001"));

        // when
        KakaoPayReadyResponse response = paymentCommandService.readyPayment(orderId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.tid()).isEqualTo("TID_NEW_0001");
        verify(paymentRepository).delete(oldPayment);
    }
}
