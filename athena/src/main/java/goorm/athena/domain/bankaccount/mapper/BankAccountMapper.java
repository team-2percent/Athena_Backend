package goorm.athena.domain.bankaccount.mapper;

import goorm.athena.domain.bankaccount.dto.req.BankAccountCreateRequest;
import goorm.athena.domain.bankaccount.dto.res.BankAccountGetResponse;
import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BankAccountMapper {
    @Mapping(target = "user", source = "user")
    @Mapping(target = "accountNumber", source = "request.accountNumber")
    @Mapping(target = "accountHolder", source = "request.accountHolder")
    @Mapping(target = "bankName", source = "request.bankName")
    BankAccount toEntity(User user, BankAccountCreateRequest request, Boolean isDefault);

    @Mapping(target = "bankAccount", source = "accountNumber")
    @Mapping(target = "isDefault", source = "default")
    BankAccountGetResponse toGetResponse(BankAccount bankAccount);
}