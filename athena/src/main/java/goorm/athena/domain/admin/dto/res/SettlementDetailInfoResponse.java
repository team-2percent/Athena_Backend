package goorm.athena.domain.admin.dto.res;

import goorm.athena.domain.project.entity.PlanName;
import goorm.athena.domain.settlement.entity.Status;

import java.time.LocalDateTime;

public record SettlementDetailInfoResponse(
        String projectTitle,
        String sellerNickname,
        Long userId,
        long targetAmount,
        long totalSales,
        long payOutAmount,
        long platformFeeTotal,
        long pgFeeTotal,
        long vatTotal,
        int totalCount,
        LocalDateTime settledAt,
        Status status,
        BankAccountInfo bankAccount,
        LocalDateTime fundingStartDate,
        LocalDateTime fundingEndDate,
        PlanName planName
) {
    public record BankAccountInfo(
            String bankName,
            String accountNumber
    ) {}
}