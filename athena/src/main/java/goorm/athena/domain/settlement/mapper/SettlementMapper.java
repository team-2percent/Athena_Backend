package goorm.athena.domain.settlement.mapper;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.settlement.entity.Settlement;
import goorm.athena.domain.settlement.entity.Status;

public class SettlementMapper {

    public static Settlement toEntity(Project project, BankAccount bankAccount, int totalCount, long totalSales, long fee, long payout) {

        return Settlement.builder()
                .user(project.getSeller())
                .project(project)
                .bankAccount(bankAccount)
                .totalCount(totalCount)
                .totalSales(totalSales)
                .platformFee(fee)
                .payOutAmount(payout)
                .status(Status.COMPLETED)
                .build();
    }
}
