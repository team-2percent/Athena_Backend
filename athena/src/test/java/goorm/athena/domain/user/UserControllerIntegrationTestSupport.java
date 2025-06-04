package goorm.athena.domain.user;

import goorm.athena.domain.user.controller.UserControllerImpl;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.global.jwt.util.LoginUserRequest;
import goorm.athena.util.IntegrationControllerTestSupport;
import org.junit.jupiter.api.BeforeEach;

public abstract class UserControllerIntegrationTestSupport extends IntegrationControllerTestSupport {
    protected UserControllerImpl controller;
    protected LoginUserRequest loginUserRequest;

    @BeforeEach
    void setUp() {
        controller = new UserControllerImpl(userService, refreshTokenService, imageGroupService, fcmTokenService);
        loginUserRequest = new LoginUserRequest("123", 1L, Role.ROLE_USER);
    }
}
