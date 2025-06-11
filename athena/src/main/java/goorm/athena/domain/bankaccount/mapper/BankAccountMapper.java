package goorm.athena.domain.bankaccount.mapper;

/*
import goorm.athena.domain.bankaccount.dto.req.BankAccountCreateRequest;
import goorm.athena.domain.bankaccount.dto.res.BankAccountCreateResponse;
import goorm.athena.domain.bankaccount.dto.res.BankAccountGetResponse;
import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.user.entity.User;

public class BankAccountMapper {
    public static BankAccount toEntity(User user, BankAccountCreateRequest request, boolean isDefault){
        return BankAccount.builder().
                user(user).
                accountNumber(request.accountNumber()).
                accountHolder(request.accountHolder()).
                bankName(request.bankName()).
                isDefault(isDefault).
                build();
    }

    public static BankAccountCreateResponse toCreateResponse(BankAccount bankAccount){
        return new BankAccountCreateResponse(
                bankAccount.getId(),
                bankAccount.getUser().getId(),
                bankAccount.getAccountNumber(),
                bankAccount.getAccountHolder(),
                bankAccount.getBankName(),
                bankAccount.isDefault()
        );
    }

    public static BankAccountGetResponse toGetResponse(BankAccount bankAccount){
        return new BankAccountGetResponse(
                bankAccount.getId(),
                bankAccount.getAccountNumber(),
                bankAccount.getAccountHolder(),
                bankAccount.getBankName(),
                bankAccount.isDefault()
        );
    }
}
 */

import goorm.athena.domain.bankaccount.dto.res.BankAccountGetResponse;
import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BankAccountMapper {
    @Mapping(target = "user", source = "user")
    BankAccount toEntity(User user, String accountNumber, String accountHolder, String bankName, boolean isDefault);

    @Mapping(target = "bankAccount", source = "accountNumber")
    BankAccountGetResponse toGetResponse(BankAccount bankAccount);
}