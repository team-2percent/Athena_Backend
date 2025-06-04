package goorm.athena.domain.comment;

import goorm.athena.domain.bankaccount.controller.BankAccountControllerImpl;
import goorm.athena.domain.comment.controller.CommentControllerImpl;
import goorm.athena.domain.comment.service.CommentService;
import goorm.athena.domain.notification.service.FcmNotificationService;
import goorm.athena.domain.project.service.ProjectService;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.global.jwt.util.LoginUserRequest;
import goorm.athena.util.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
public abstract class CommentControllerIntegrationSupport extends IntegrationTestSupport {
    protected CommentControllerImpl controller;
    protected LoginUserRequest loginUserRequest;

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected CommentService commentService;

    @MockBean
    protected ProjectService projectService;

    @MockBean
    protected UserService userService;

    @MockBean
    protected FcmNotificationService fcmNotificationService;

    @BeforeEach
    void setUp() {
        controller = new CommentControllerImpl(commentService, projectService, fcmNotificationService);
        loginUserRequest = new LoginUserRequest("123", 1L, Role.ROLE_USER);
    }
}
