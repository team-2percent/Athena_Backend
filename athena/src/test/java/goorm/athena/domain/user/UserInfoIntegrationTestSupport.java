package goorm.athena.domain.user;

import goorm.athena.domain.comment.service.CommentService;
import goorm.athena.domain.user.controller.UserInfoControllerImpl;
import goorm.athena.domain.user.service.MyInfoService;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.domain.userCoupon.service.UserCouponService;
import goorm.athena.util.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

public abstract class UserInfoIntegrationTestSupport extends IntegrationTestSupport {
    @Mock
    protected UserService userService;

    @Mock
    protected CommentService commentService;

    @Mock
    protected UserCouponService userCouponService;

    @Mock
    protected MyInfoService myInfoService;

    @InjectMocks
    protected UserInfoControllerImpl userInfoController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
}
