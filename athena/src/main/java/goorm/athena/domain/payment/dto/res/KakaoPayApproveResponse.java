package goorm.athena.domain.payment.dto.res;

import java.time.LocalDateTime;

public record KakaoPayApproveResponse(
        String aid,
        String tid,
        String cid,
        String paymentMethodType,
        Amount amount,
        String itemName,
        String itemCode,
        int quantity,
        LocalDateTime approvedAt
) {}
