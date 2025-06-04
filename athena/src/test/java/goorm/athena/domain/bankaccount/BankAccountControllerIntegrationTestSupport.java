package goorm.athena.domain.bankaccount;

import goorm.athena.domain.bankaccount.controller.BankAccountControllerImpl;
import goorm.athena.domain.bankaccount.service.BankAccountService;
import goorm.athena.domain.deliveryinfo.controller.DeliveryInfoControllerImpl;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.global.jwt.util.LoginUserRequest;
import goorm.athena.util.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
public abstract class BankAccountControllerIntegrationTestSupport extends IntegrationTestSupport {
    protected BankAccountControllerImpl controller;
    protected LoginUserRequest loginUserRequest;

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected BankAccountService bankAccountService;

    @MockBean
    protected UserService userService;

    @BeforeEach
    void setUp() {
        controller = new BankAccountControllerImpl(bankAccountService);
        loginUserRequest = new LoginUserRequest("123", 1L, Role.ROLE_USER);
    }
}
