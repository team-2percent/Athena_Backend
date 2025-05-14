package goorm.athena.domain.admin.dto.res;

import goorm.athena.domain.settlement.dto.res.SettlementSummaryResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public record SettlementSummaryPageResponse(
        List<SettlementSummaryResponse> content,
        PageInfo pageInfo
) {
    public static SettlementSummaryPageResponse of(Page<SettlementSummaryResponse> page) {
        return new SettlementSummaryPageResponse(
                page.getContent(),
                new PageInfo(page.getNumber(), page.getTotalPages())
        );
    }

    public record PageInfo(
            int currentPage,
            int totalPages
    ) {}
}