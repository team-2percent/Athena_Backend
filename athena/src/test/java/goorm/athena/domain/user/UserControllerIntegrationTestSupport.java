package goorm.athena.domain.user;

import goorm.athena.domain.comment.service.CommentService;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.notification.service.FcmTokenService;
import goorm.athena.domain.user.controller.UserControllerImpl;
import goorm.athena.domain.user.controller.UserInfoControllerImpl;
import goorm.athena.domain.user.service.MyInfoService;
import goorm.athena.domain.user.service.RefreshTokenService;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.domain.userCoupon.service.UserCouponService;
import goorm.athena.util.IntegrationTestSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindingResult;

public abstract class UserControllerIntegrationTestSupport extends IntegrationTestSupport {
    @Mock
    protected UserService userService;

    @Mock
    protected RefreshTokenService refreshTokenService;

    @Mock
    protected ImageGroupService imageGroupService;

    @Mock
    protected FcmTokenService fcmTokenService;

    @Mock
    protected HttpServletResponse httpServletResponse;

    @Mock
    protected BindingResult bindingResult;

    @InjectMocks
    protected UserControllerImpl userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
}
