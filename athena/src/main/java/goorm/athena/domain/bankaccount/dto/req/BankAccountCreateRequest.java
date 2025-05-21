package goorm.athena.domain.bankaccount.dto.req;

public record BankAccountCreateRequest (
        String accountNumber,
        String accountHolder,
        String bankName
) { }