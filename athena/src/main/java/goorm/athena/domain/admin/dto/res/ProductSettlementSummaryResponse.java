package goorm.athena.domain.admin.dto.res;

import java.util.List;

public record ProductSettlementSummaryResponse(
        List<Item> items,
        Summary total
) {
    public record Item(
            String productName,
            long totalQuantity,
            long totalPrice,
            long platformFee,
            long pgFee,
            long vat,
            long payoutAmount
    ) {}

    public record Summary(
            long totalQuantity,
            long totalPrice,
            long platformFee,
            long pgFee,
            long vat,
            long payoutAmount
    ) {}
}