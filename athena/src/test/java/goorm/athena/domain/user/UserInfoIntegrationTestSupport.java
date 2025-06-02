package goorm.athena.domain.user;

import goorm.athena.domain.comment.service.CommentService;
import goorm.athena.domain.user.controller.UserInfoControllerImpl;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.service.MyInfoService;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.domain.userCoupon.service.UserCouponService;
import goorm.athena.global.jwt.util.LoginUserRequest;
import goorm.athena.util.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
public abstract class UserInfoIntegrationTestSupport extends IntegrationTestSupport {
    protected UserInfoControllerImpl controller;
    protected LoginUserRequest request;

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected UserService userService;

    @MockBean
    protected CommentService commentService;

    @MockBean
    protected UserCouponService userCouponService;

    @MockBean
    protected MyInfoService myInfoService;

    @BeforeEach
    void setUp() {
        controller = new UserInfoControllerImpl(commentService, myInfoService, userService, userCouponService);
        request = new LoginUserRequest("123", 1L, Role.ROLE_USER);
    }
}
