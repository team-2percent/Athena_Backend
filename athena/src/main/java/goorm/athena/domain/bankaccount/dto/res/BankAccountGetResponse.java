package goorm.athena.domain.bankaccount.dto.res;

public record BankAccountGetResponse(
        Long id,
        String bankAccount,
        String accountHolder,
        String bankName,
        boolean isDefault
) {
}
