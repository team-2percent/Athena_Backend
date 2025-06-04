package goorm.athena.util;

import goorm.athena.domain.bankaccount.service.BankAccountService;
import goorm.athena.domain.comment.service.CommentService;
import goorm.athena.domain.deliveryinfo.service.DeliveryInfoService;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.notification.service.FcmNotificationService;
import goorm.athena.domain.notification.service.FcmTokenService;
import goorm.athena.domain.project.service.ProjectService;
import goorm.athena.domain.user.service.MyInfoService;
import goorm.athena.domain.user.service.RefreshTokenService;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.domain.userCoupon.service.UserCouponService;
import goorm.athena.global.jwt.util.JwtTokenizer;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
public abstract class IntegrationControllerTestSupport {
    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected DeliveryInfoService deliveryInfoService;

    @MockBean
    protected UserService userService;

    @MockBean
    protected RefreshTokenService refreshTokenService;

    @MockBean
    protected JwtTokenizer jwtTokenizer;

    @MockBean
    protected ImageGroupService imageGroupService;

    @MockBean
    protected FcmTokenService fcmTokenService;

    @MockBean
    protected HttpServletResponse httpServletResponse;

    @MockBean
    protected BindingResult bindingResult;

    @MockBean
    protected CommentService commentService;

    @MockBean
    protected UserCouponService userCouponService;

    @MockBean
    protected MyInfoService myInfoService;

    @MockBean
    protected BankAccountService bankAccountService;

    @MockBean
    protected ProjectService projectService;

    @MockBean
    protected FcmNotificationService fcmNotificationService;
}
