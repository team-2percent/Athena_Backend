package goorm.athena.domain.payment.dto.res;

public record KakaoPayReadyResponse(
        String next_redirect_pc_url,
        String tid
) {}