package goorm.athena.domain.settlement.dto.res;

import goorm.athena.domain.settlement.entity.Status;

import java.time.LocalDateTime;

public record SettlementSummaryResponse(
        Long settlementId,
        String projectTitle,
        long totalSales,
        long platformFee,
        long payOutAmount,
        String sellerName,
        LocalDateTime requestedAt,
        Status status
) {}