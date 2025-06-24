//package goorm.athena.domain.payment.Infra;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import goorm.athena.domain.payment.dto.req.KakaoPayApproveRequest;
//import goorm.athena.domain.payment.dto.req.PaymentApproveRequest;
//import goorm.athena.domain.payment.dto.req.PaymentReadyRequest;
//import goorm.athena.domain.payment.dto.res.KakaoPayApproveResponse;
//import goorm.athena.domain.payment.dto.res.KakaoPayReadyResponse;
//import goorm.athena.domain.payment.entity.Payment;
//import goorm.athena.domain.payment.event.KakaoPayApproveEvent;
//import goorm.athena.domain.user.entity.User;
//import goorm.athena.global.exception.CustomException;
//import goorm.athena.global.exception.ErrorCode;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Optional;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class KakaoPayImpl implements KakaoPay {
//
//    private final RestTemplate restTemplate;
//    private final ObjectMapper objectMapper;
//
//    @Value("${spring.kakao.api.cid}")
//    private String cid;
//
//    @Value("${spring.kakao.api.admin-key}")
//    private String adminKey;
//
//    @Value("${spring.kakao.api.approval-url}")
//    private String approvalUrl;
//
//    @Value("${spring.kakao.api.cancel-url}")
//    private String cancelUrl;
//
//    @Value("${spring.kakao.api.fail-url}")
//    private String failUrl;
//
//    private static final String KAKAO_PAY_READY_URL = "https://open-api.kakaopay.com/online/v1/payment/ready";
//    private static final String KAKAO_PAY_APPROVE_URL = "https://open-api.kakaopay.com/online/v1/payment/approve";
//
//    @Override
//    public KakaoPayReadyResponse requestKakaoPayment(PaymentReadyRequest requestDto, User user, Long orderId) {
//        HttpEntity<String> entity = createPaymentRequestEntity(requestDto, user, orderId);
//        log.info("adminKey 확인: {}", adminKey);
//
//
//        try {
//            ResponseEntity<KakaoPayReadyResponse> response = restTemplate.exchange(
//                    KAKAO_PAY_READY_URL,
//                    HttpMethod.POST,
//                    entity,
//                    KakaoPayReadyResponse.class
//            );
//
////            String tid = response.getBody().tid();
//            log.info("카카오페이 결제 요청 성공");
//
//            return response.getBody();
//        } catch (Exception e) {
//            log.error("카카오페이 결제 요청 실패", e);
//            throw new CustomException(ErrorCode.KAKAO_PAY_REQUEST_FAILED);
//        }
//    }
//
//    @Override
//    public KakaoPayApproveResponse approveKakaoPayment(KakaoPayApproveEvent event) {
//
//        HttpEntity<String> entity = createPaymentApproveEntity(event);
//
//        ResponseEntity<KakaoPayApproveResponse> response = restTemplate.exchange(
//                KAKAO_PAY_APPROVE_URL,
//                HttpMethod.POST,
//                entity,
//                KakaoPayApproveResponse.class
//            );
//        if (response.getStatusCode() != HttpStatus.OK) {
//            throw new CustomException(ErrorCode.KAKAO_PAY_APPROVE_FAILED);
//        }
//
//        return response.getBody();
//
//    }
//
//    private HttpEntity<String> createPaymentRequestEntity(PaymentReadyRequest dto, User user, Long orderId) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "SECRET_KEY " + adminKey);
//        headers.set("Content-Type", "application/json");
//
//        Map<String, Object> params = new HashMap<>();
//        params.put("cid", cid);
//        params.put("partner_order_id", orderId);
//        params.put("partner_user_id", user.getId());
//        params.put("item_name", dto.projectName());
//        params.put("quantity", dto.quantity());
//        params.put("total_amount", dto.totalAmount());
//        params.put("tax_free_amount", 0);
//
//        params.put("approval_url", approvalUrl + "/" + orderId);
//        params.put("cancel_url", cancelUrl);
//        params.put("fail_url", failUrl);
//
//        return createHttpEntity(params);
//    }
//
//    private HttpEntity<String> createPaymentApproveEntity(KakaoPayApproveEvent event) {
//        Payment payment = event.getPayment();
//        String pgToken = event.getPgToken();
//
//        Map<String, Object> params = new HashMap<>();
//        params.put("cid", cid);
//        params.put("tid", payment.getTid());
//        params.put("partner_order_id", payment.getOrder().getId());
//        params.put("partner_user_id", payment.getUser().getId());
//        params.put("pg_token", pgToken);
//
//        return createHttpEntity(params);
//    }
//
//    private HttpEntity<String> createHttpEntity(Map<String, Object> params) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "SECRET_KEY " + adminKey);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        try {
//            String body = objectMapper.writeValueAsString(params);
//            return new HttpEntity<>(body, headers);
//        } catch (JsonProcessingException e) {
//            throw new CustomException(ErrorCode.JSON_PROCESSING_ERROR);
//        }
//    }
//
//
//}
