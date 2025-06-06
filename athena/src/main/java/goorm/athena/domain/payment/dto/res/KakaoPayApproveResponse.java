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
        LocalDateTime approvedAt,
        String message,
        String redirectUrl
) {
    public static KakaoPayApproveResponse ofSuccess(KakaoPayApproveResponse origin) {
        return new KakaoPayApproveResponse(
                origin.aid(),
                origin.tid(),
                origin.cid(),
                origin.paymentMethodType(),
                origin.amount(),
                origin.itemName(),
                origin.itemCode(),
                origin.quantity(),
                origin.approvedAt(),
                "결제가 성공적으로 완료되었습니다.",
                "/payment/complete"
        );
    }

    public static KakaoPayApproveResponse ofFailure(String message) {
        return new KakaoPayApproveResponse(
                null, null, null, null, null, null, null, 0, null,
                message,
                "/payment/fail"
        );
    }

    public static KakaoPayApproveResponse ofFailure() {
        return ofFailure("카카오 결제 승인에 실패했습니다.");
    }

}
