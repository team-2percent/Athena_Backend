package goorm.athena.domain.settlement.mapper;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.settlement.entity.Settlement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SettlementMapper {

    @Mapping(target = "project", source = "project")
    @Mapping(target = "user", expression = "java(project.getSeller())")
    @Mapping(target = "status", constant = "PENDING")
    Settlement toEntity(Project project, BankAccount bankAccount,
                        int totalCount, long totalSales,
                        long platformFeeTotal, long pgFeeTotal, long vatTotal,
                        long payOutAmount);

}
