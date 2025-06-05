package goorm.athena.domain.deliveryinfo;

import goorm.athena.domain.deliveryinfo.controller.DeliveryInfoControllerImpl;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.global.jwt.util.LoginUserRequest;
import goorm.athena.util.IntegrationControllerTestSupport;
import org.junit.jupiter.api.BeforeEach;

public abstract class DeliveryControllerIntegrationTestSupport extends IntegrationControllerTestSupport {
    protected DeliveryInfoControllerImpl controller;
    protected LoginUserRequest loginUserRequest;
    

    @BeforeEach
    void setUp() {
        controller = new DeliveryInfoControllerImpl(deliveryInfoService);
        loginUserRequest = new LoginUserRequest("123", 1L, Role.ROLE_USER);
    }
}
