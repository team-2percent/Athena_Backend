package goorm.athena.domain.payment.dto.req;

public record KakaoPayApproveRequest(
        String tid,
        Long orderId,
        String pgToken,
        Long userId
) {}