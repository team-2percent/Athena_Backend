package goorm.athena.domain.admin.dto.res;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record SettlementHistoryPageResponse(
        List<SettlementHistoryItem> content,
        PageInfo pageInfo
) {
    public record SettlementHistoryItem(
            String productName,
            Integer quantity,
            Long totalPrice,
            Integer fee,
            Long amount,
            LocalDateTime orderedAt
    ) {}

    public record PageInfo(int currentPage, int totalPages) {}
}