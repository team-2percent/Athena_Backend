package goorm.athena.domain.payment.dto.req;

public record PaymentApproveRequest(
        Long orderId,
        String pgToken
) {}