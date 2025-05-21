package goorm.athena.domain.settlement.mapper;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.settlement.entity.Settlement;
import goorm.athena.domain.settlement.entity.Status;

public class SettlementMapper {

    public static Settlement toEntity(Project project, BankAccount bankAccount,
                                      int totalCount, long totalSales,
                                      long platformFeeTotal, long pgFeeTotal, long vatTotal,
                                      long payOutAmount) {
        return Settlement.builder()
                .project(project)
                .user(project.getSeller())
                .bankAccount(bankAccount)
                .totalCount(totalCount)
                .totalSales(totalSales)
                .platformFeeTotal(platformFeeTotal)
                .pgFeeTotal(pgFeeTotal)
                .vatTotal(vatTotal)
                .payOutAmount(payOutAmount)
                .status(Status.PENDING)
                .build();
    }
}
