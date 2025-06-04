package goorm.athena.domain.user;

import goorm.athena.domain.user.controller.UserInfoControllerImpl;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.global.jwt.util.LoginUserRequest;
import goorm.athena.util.IntegrationControllerTestSupport;
import org.junit.jupiter.api.BeforeEach;

public abstract class UserInfoIntegrationTestSupport extends IntegrationControllerTestSupport {
    protected UserInfoControllerImpl controller;
    protected LoginUserRequest request;

    @BeforeEach
    void setUp() {
        controller = new UserInfoControllerImpl(commentService, myInfoService, userService, userCouponService);
        request = new LoginUserRequest("123", 1L, Role.ROLE_USER);
    }
}
