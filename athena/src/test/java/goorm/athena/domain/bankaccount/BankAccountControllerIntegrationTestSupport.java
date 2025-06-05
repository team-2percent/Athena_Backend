package goorm.athena.domain.bankaccount;

import goorm.athena.domain.bankaccount.controller.BankAccountControllerImpl;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.global.jwt.util.LoginUserRequest;
import goorm.athena.util.IntegrationControllerTestSupport;
import org.junit.jupiter.api.BeforeEach;

public abstract class BankAccountControllerIntegrationTestSupport extends IntegrationControllerTestSupport {
    protected BankAccountControllerImpl controller;
    protected LoginUserRequest loginUserRequest;

    @BeforeEach
    void setUp() {
        controller = new BankAccountControllerImpl(bankAccountService);
        loginUserRequest = new LoginUserRequest("123", 1L, Role.ROLE_USER);
    }
}
