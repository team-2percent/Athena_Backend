package goorm.athena.domain.bankaccount.dto.res;

public record BankAccountGetResponse(
        String accountNumber,
        String accountHolder,
        String bankName,
        boolean isDefault
) {
}
