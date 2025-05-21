package goorm.athena.domain.bankaccount.dto.res;

public record BankAccountCreateResponse(
        Long id,
        Long userId,
        String accountNumber,
        String accountHolder,
        String bankName,
        boolean isDefault
) {
}
