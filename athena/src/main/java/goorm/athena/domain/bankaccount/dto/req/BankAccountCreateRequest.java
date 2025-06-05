package goorm.athena.domain.bankaccount.dto.req;

import jakarta.persistence.Column;

public record BankAccountCreateRequest (
        @Column(length = 50)
        String accountNumber,

        @Column(length = 50)
        String accountHolder,

        @Column(length = 50)
        String bankName
) { }