package goorm.athena.domain.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import goorm.athena.domain.payment.dto.req.PaymentApproveRequest;
import goorm.athena.domain.payment.dto.req.PaymentReadyRequest;
import goorm.athena.domain.payment.dto.res.KakaoPayApproveResponse;
import goorm.athena.domain.payment.dto.res.KakaoPayReadyResponse;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoPayService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.kakao.api.cid}")
    private String cid;

    @Value("${spring.kakao.api.admin-key}")
    private String adminKey;

    @Value("${spring.kakao.api.approval-url}")
    private String approvalUrl;

    @Value("${spring.kakao.api.cancel-url}")
    private String cancelUrl;

    @Value("${spring.kakao.api.fail-url}")
    private String failUrl;

    public KakaoPayReadyResponse requestKakaoPayment(PaymentReadyRequest requestDto, User user, Long orderId) {
        HttpEntity<String> entity = createPaymentRequestEntity(requestDto, user, orderId);
        log.info("adminKey 확인: {}", adminKey);


        try {
            ResponseEntity<KakaoPayReadyResponse> response = restTemplate.exchange(
                    "https://open-api.kakaopay.com/online/v1/payment/ready",
                    HttpMethod.POST,
                    entity,
                    KakaoPayReadyResponse.class
            );

//            String tid = response.getBody().tid();
            log.info("카카오페이 결제 요청 성공");

            return response.getBody();
        } catch (Exception e) {
            log.error("카카오페이 결제 요청 실패", e);
            throw new CustomException(ErrorCode.KAKAO_PAY_REQUEST_FAILED);
        }
    }

    public KakaoPayApproveResponse approveKakaoPayment(String tid, PaymentApproveRequest requestDto, User user) {
        HttpEntity<String> entity = createPaymentApproveEntity(requestDto, user, tid);

        try {
            ResponseEntity<KakaoPayApproveResponse> response = restTemplate.exchange(
                    "https://open-api.kakaopay.com/online/v1/payment/approve",
                    HttpMethod.POST,
                    entity,
                    KakaoPayApproveResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("카카오페이 승인 실패", e);
            throw new CustomException(ErrorCode.KAKAO_PAY_APPROVE_FAILED);
        }
    }

    private HttpEntity<String> createPaymentRequestEntity(PaymentReadyRequest dto, User user, Long orderId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SECRET_KEY " + adminKey);
        headers.set("Content-Type", "application/json");

        Map<String, Object> params = new HashMap<>();
        params.put("cid", cid);
        params.put("partner_order_id", orderId);
        params.put("partner_user_id", user.getId());
        params.put("item_name", dto.projectName());
        params.put("quantity", dto.quantity());
        params.put("total_amount", dto.totalAmount());
        params.put("tax_free_amount", 0);

        params.put("approval_url", approvalUrl + "/" + orderId);
        params.put("cancel_url", cancelUrl);
        params.put("fail_url", failUrl);

        return createHttpEntity(params);
    }

    private HttpEntity<String> createPaymentApproveEntity(PaymentApproveRequest dto, User user, String tid) {
        Map<String, Object> params = new HashMap<>();
        params.put("cid", cid);
        params.put("tid", tid);
        params.put("partner_order_id", dto.orderId());
        params.put("partner_user_id", user.getId());
        params.put("pg_token", dto.pgToken());

        return createHttpEntity(params);
    }

    private HttpEntity<String> createHttpEntity(Map<String, Object> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SECRET_KEY " + adminKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            String body = objectMapper.writeValueAsString(params);
            return new HttpEntity<>(body, headers);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.JSON_PROCESSING_ERROR);
        }
    }


}