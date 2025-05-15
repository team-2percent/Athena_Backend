package goorm.athena.domain.admin.dto.res;

import goorm.athena.domain.settlement.entity.Status;

import java.time.LocalDateTime;

public record SettlementDetailInfoResponse(
        String projectTitle,
        String sellerNickname,
        Long userId,
        long targetAmount,
        long totalSales,
        long payoutAmount,
        long platformFee,
        int totalCount,
        LocalDateTime settledAt,
        Status status,
        BankAccountInfo bankAccount,
        LocalDateTime fundingStartDate,
        LocalDateTime fundingEndDate
) {
    public record BankAccountInfo(
            String bankName,
            String accountNumber
    ) {}
}