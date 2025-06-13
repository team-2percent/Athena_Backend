package goorm.athena.domain.comment.service;

import goorm.athena.domain.comment.CommentIntegrationSupport;
import goorm.athena.domain.comment.dto.res.CommentCreateResponse;
import goorm.athena.domain.comment.dto.res.CommentGetResponse;
import goorm.athena.domain.comment.entity.Comment;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Transactional
class CommentServiceTest extends CommentIntegrationSupport {

    @DisplayName("16번 유저가 16번 프로젝트에 코멘트를 이미 작성했다면 ALREADY_COMMENTED 에러를 리턴한다.")
    @Test
    void createComment_Error() {
        // given
        User user = userRepository.findById(16L).get();
        Project project = projectRepository.findById(16L).get();
        Comment comment = setupComment(user, project, "123");

        commentRepository.save(comment);

        Optional<Project> project1 = projectRepository.findById(project.getId());

        // when & then
        assertThatThrownBy(() -> commentCommandService.createComment(project1.get().getId(), user.getId(), "123"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.ALREADY_COMMENTED.getErrorMessage());

    }

    @DisplayName("로그인 한 20번 유저가 17번 프로젝트에 입력한 content로 후기를 작성한다.")
    @Test
    void createComment() {
        // given
        User user = userRepository.findById(20L).get();
        Project project = projectRepository.findById(17L).get();

        // when
        CommentCreateResponse response = commentCommandService.createComment(project.getId(), user.getId(), "123123");

        // then
        assertThat(response.userName()).isEqualTo(user.getNickname());
        assertThat(response.content()).isEqualTo("123123");
    }

    @DisplayName("18번, 19번 유저가 18번 프로젝트에 후기를 등록하고 프로젝트에 후기 목록을 조회하면, " +
            "유저 닉네임, 내용과 함께 후기 목록을 조회한다.")
    @Test
    void getCommentByProject() {
        // given
        User user = userRepository.findById(18L).get();
        User user2 = userRepository.findById(19L).get();
        Project project = projectRepository.findById(18L).get();

        Comment comment = setupComment(user, project, "123");
        Comment comment1 = setupComment(user2, project, "12#");

        commentRepository.saveAll(List.of(comment, comment1));

        // when
        List<CommentGetResponse> response = commentQueryService.getCommentByProject(project.getId());
        String expectedImageUrl = imageQueryService.getImage(user.getImageGroup().getId());
        String expectedImageUrl2 = imageQueryService.getImage(user2.getImageGroup().getId());

        // then
        assertThat(response).hasSize(2);

        CommentGetResponse commentResponse = response.get(0);
        assertThat(commentResponse.content()).isEqualTo("123");
        assertThat(commentResponse.userName()).isEqualTo(user.getNickname());
        assertThat(commentResponse.imageUrl()).isEqualTo(expectedImageUrl);

        CommentGetResponse commentResponse2 = response.get(1);
        assertThat(commentResponse2.content()).isEqualTo("12#");
        assertThat(commentResponse2.userName()).isEqualTo(user2.getNickname());
        assertThat(commentResponse2.imageUrl()).isEqualTo(expectedImageUrl2);
    }

    @DisplayName("20번, 21번 유저가 19번 프로젝트에 후기를 작성하고 20번 유저가 프로젝트의 후기 목록을 조회하면" +
            "20번 유저가 작성했던 후기들을 프로젝트 정보와 함께 조회한다.")
    @Test
    void getCommentByUser() {
        // given
        User user = userRepository.findById(20L).get();
        User user2 = userRepository.findById(21L).get();
        Project project = projectRepository.findById(19L).get();
        Comment comment = setupComment(user, project, "123");
        Comment comment1 = setupComment(user2, project, "12#");

        commentRepository.saveAll(List.of(comment, comment1));

        // when
        List<CommentGetResponse> response = commentQueryService.getCommentByUser(user.getId());
        String expectedImageUrl = imageQueryService.getImage(user.getImageGroup().getId());

        // then
        assertThat(response).hasSize(1);

        CommentGetResponse commentResponse = response.get(0);
        assertThat(commentResponse.content()).isEqualTo("123");
        assertThat(commentResponse.userName()).isEqualTo(user.getNickname());

        assertThat(commentResponse.imageUrl()).isEqualTo(expectedImageUrl);
    }
}