package goorm.athena.domain.comment.service;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.comment.CommentIntegrationSupport;
import goorm.athena.domain.comment.dto.res.CommentCreateResponse;
import goorm.athena.domain.comment.dto.res.CommentGetResponse;
import goorm.athena.domain.comment.entity.Comment;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.project.entity.PlatformPlan;
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

    @DisplayName("유저가 해당 프로젝트에 코멘트를 이미 작성했다면 에러를 리턴한다.")
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

    @DisplayName("유저가 해당 프로젝트에 후기를 작성한다.")
    @Test
    void createComment() {
        // given
        User user = userRepository.findById(17L).get();
        Project project = projectRepository.findById(17L).get();

        // when
        CommentCreateResponse response = commentCommandService.createComment(project.getId(), user.getId(), "123123");

        // then
        assertThat(response.userName()).isEqualTo(user.getNickname());
        assertThat(response.content()).isEqualTo("123123");
    }

    @DisplayName("프로젝트 ID로 조회하여 등록된 후기들을 유저 닉네임과 함께 조회한다.")
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

        // then
        assertThat(response).hasSize(2);

        CommentGetResponse commentResponse = response.get(0);
        assertThat(commentResponse.content()).isEqualTo("123");
        assertThat(commentResponse.userName()).isEqualTo(user.getNickname());

        assertThat(commentResponse.imageUrl()).isEqualTo(expectedImageUrl);
    }

    @DisplayName("로그인 한 유저가 자신이 작성한 후기들을 프로젝트 정보와 함께 조회한다.")
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