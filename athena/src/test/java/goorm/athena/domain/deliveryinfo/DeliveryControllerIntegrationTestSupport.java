package goorm.athena.domain.deliveryinfo;

import goorm.athena.domain.deliveryinfo.controller.DeliveryInfoControllerImpl;
import goorm.athena.domain.deliveryinfo.service.DeliveryInfoService;
import goorm.athena.domain.user.controller.UserControllerImpl;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.global.jwt.util.LoginUserRequest;
import goorm.athena.util.IntegrationControllerTestSupport;
import goorm.athena.util.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

public abstract class DeliveryControllerIntegrationTestSupport extends IntegrationControllerTestSupport {
    protected DeliveryInfoControllerImpl controller;
    protected LoginUserRequest loginUserRequest;
    

    @BeforeEach
    void setUp() {
        controller = new DeliveryInfoControllerImpl(deliveryInfoService);
        loginUserRequest = new LoginUserRequest("123", 1L, Role.ROLE_USER);
    }
}
