package goorm.athena.domain.comment;

import goorm.athena.domain.comment.controller.CommentControllerImpl;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.global.jwt.util.LoginUserRequest;
import goorm.athena.util.IntegrationControllerTestSupport;
import org.junit.jupiter.api.BeforeEach;

public abstract class CommentControllerIntegrationSupport extends IntegrationControllerTestSupport {
    protected CommentControllerImpl controller;
    protected LoginUserRequest loginUserRequest;


    @BeforeEach
    void setUp() {
        controller = new CommentControllerImpl(commentService, projectService, fcmNotificationService);
        loginUserRequest = new LoginUserRequest("123", 1L, Role.ROLE_USER);
    }
}
