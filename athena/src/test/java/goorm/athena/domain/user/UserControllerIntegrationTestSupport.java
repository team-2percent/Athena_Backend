package goorm.athena.domain.user;

import goorm.athena.domain.comment.service.CommentService;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.notification.service.FcmTokenService;
import goorm.athena.domain.user.controller.UserControllerImpl;
import goorm.athena.domain.user.controller.UserInfoControllerImpl;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.service.MyInfoService;
import goorm.athena.domain.user.service.RefreshTokenService;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.domain.userCoupon.service.UserCouponService;
import goorm.athena.global.jwt.util.LoginUserRequest;
import goorm.athena.util.IntegrationTestSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;

@AutoConfigureMockMvc
public abstract class UserControllerIntegrationTestSupport extends IntegrationTestSupport {
    protected UserControllerImpl controller;
    protected LoginUserRequest loginUserRequest;

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected UserService userService;

    @MockBean
    protected RefreshTokenService refreshTokenService;

    @MockBean
    protected ImageGroupService imageGroupService;

    @MockBean
    protected FcmTokenService fcmTokenService;

    @MockBean
    protected HttpServletResponse httpServletResponse;

    @MockBean
    protected BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        controller = new UserControllerImpl(userService, refreshTokenService, imageGroupService, fcmTokenService);
        loginUserRequest = new LoginUserRequest("123", 1L, Role.ROLE_USER);
    }
}
