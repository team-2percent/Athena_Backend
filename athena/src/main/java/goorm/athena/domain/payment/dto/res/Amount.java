package goorm.athena.domain.payment.dto.res;

public record Amount(
        int total,
        int taxFree,
        int vat,
        int point,
        int discount
) {}