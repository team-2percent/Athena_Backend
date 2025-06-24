package goorm.athena.domain.payment.Infra.V1;


import com.fasterxml.jackson.databind.ObjectMapper;
import goorm.athena.domain.payment.Infra.KakaoPay;
import goorm.athena.domain.payment.dto.req.PaymentReadyRequest;
import goorm.athena.domain.payment.dto.res.KakaoPayApproveResponse;
import goorm.athena.domain.payment.dto.res.KakaoPayReadyResponse;
import goorm.athena.domain.payment.event.KakaoPayApproveEvent;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoPayImpl1 implements KakaoPay {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.kakao.api.base-url}")
    private String baseUrl;

    @Override
    public KakaoPayReadyResponse requestKakaoPayment(PaymentReadyRequest dto, User user, Long orderId) {
        String url = baseUrl + "/ready";

        Map<String, Object> params = new HashMap<>();
        params.put("cid", "TC0ONETIME");
        params.put("partner_order_id", orderId);
        params.put("partner_user_id", user.getId());
        params.put("item_name", dto.projectName());
        params.put("quantity", dto.quantity());
        params.put("total_amount", dto.totalAmount());

        KakaoPayReadyResponse response = sendPost(url, params, KakaoPayReadyResponse.class);

        return new KakaoPayReadyResponse(
                response.next_redirect_pc_url(),
                response.tid()
//                orderId
        );
    }

    @Override
    public KakaoPayApproveResponse approveKakaoPayment(KakaoPayApproveEvent event) {
        String url = baseUrl + "/approve";

        Map<String, Object> params = new HashMap<>();
        params.put("pg_token", event.getPgToken());

        return sendPost(url, params, KakaoPayApproveResponse.class);
    }

    private <T> T sendPost(String url, Map<String, Object> params, Class<T> clazz) {
        try {
            String body = objectMapper.writeValueAsString(params);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            log.info("Mock 카카오 서버 요청: {}", body);
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.POST, entity, clazz);
            return response.getBody();
        } catch (Exception e) {
            log.error("Mock 카카오페이 요청 실패", e);
            throw new CustomException(ErrorCode.KAKAO_PAY_REQUEST_FAILED);
        }
    }
}