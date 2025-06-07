package goorm.athena.domain.user;

import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.user.controller.RefreshControllerImpl;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.domain.user.service.RefreshTokenService;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.global.jwt.util.JwtTokenizer;
import goorm.athena.global.jwt.util.LoginUserRequest;
import goorm.athena.util.IntegrationControllerTestSupport;
import goorm.athena.util.TestEntityFactory;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class RefreshControllerIntegrationTestSupport extends IntegrationControllerTestSupport {
    @Autowired
    protected RefreshControllerImpl controller;
    protected LoginUserRequest loginUserRequest;

    @Autowired
    protected RefreshTokenService refreshTokenService;

    @Autowired
    protected UserService userService;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected JwtTokenizer jwtTokenizer;

    @BeforeEach
    void setUp() {
        controller = new RefreshControllerImpl(refreshTokenService,  jwtTokenizer);
        loginUserRequest = new LoginUserRequest("123", 1L, Role.ROLE_USER);
    }

    protected User setupUser(String email, String password, String nickname, ImageGroup imageGroup) {
        User user = TestEntityFactory.createUser(email, password, nickname, imageGroup, Role.ROLE_USER);
        return user;
    }


}
