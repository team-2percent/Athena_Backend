package goorm.athena.domain.comment.controller;

import goorm.athena.domain.comment.CommentControllerIntegrationSupport;
import goorm.athena.domain.comment.dto.req.CommentCreateRequest;
import goorm.athena.domain.comment.dto.res.CommentCreateResponse;
import goorm.athena.domain.comment.dto.res.CommentGetResponse;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.global.jwt.util.LoginUserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommentControllerImplTest extends CommentControllerIntegrationSupport {

    @DisplayName("로그인 한 사용자가 해당 프로젝트에 코멘트를 작성한다.")
    @Test
    void createComment() {
        // given
        Long projectId = 1L;
        String content = "123";
        LoginUserRequest loginUserRequest = new LoginUserRequest("123", 1L, Role.ROLE_USER);
        CommentCreateRequest request = new CommentCreateRequest(1L, content);
        CommentCreateResponse fakeResponse = new CommentCreateResponse(1L, "!23", "123", LocalDateTime.now());

        Project project = new Project();
        ReflectionTestUtils.setField(project, "title", "Test 프로젝트 제목");
        when(projectService.getById(projectId)).thenReturn(project);

        // when
        when(commentService.createComment(projectId, loginUserRequest.userId(), content))
                .thenReturn(fakeResponse);

        ResponseEntity<CommentCreateResponse> response = controller.createComment(loginUserRequest, request);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(content, response.getBody().content()); // 결과 검증
        verify(commentService).createComment(projectId, loginUserRequest.userId(), content); // 호출 여부 검증
    }

    @DisplayName("해당 프로젝트의 코멘트 목록들을 조회한다. ")
    @Test
    void getComment() {
        Long projectId = 1L;

        Project project = new Project();
        ReflectionTestUtils.setField(project, "title", "Test 프로젝트 제목");
        when(projectService.getById(projectId)).thenReturn(project);

        CommentGetResponse comment1 = new CommentGetResponse(
                1L, "123" ,"123" ,"123", LocalDateTime.now(), projectId, "!@#"
        );

        CommentGetResponse comment2 = new CommentGetResponse(
                2L, "124" ,"123" ,"123", LocalDateTime.now(), projectId, "!@#"
        );

        List<CommentGetResponse> expectedResponse = List.of(comment1, comment2);

        when(commentService.getCommentByProject(projectId)).thenReturn(expectedResponse);

        // when
        ResponseEntity<List<CommentGetResponse>> responses = controller.getComment(projectId);

        // then
        assertEquals(200, responses.getStatusCodeValue());
        assertEquals(expectedResponse, responses.getBody());
        verify(commentService).getCommentByProject(projectId);
    }
}