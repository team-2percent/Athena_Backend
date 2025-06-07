package goorm.athena.domain.comment.service;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.comment.CommentIntegrationSupport;
import goorm.athena.domain.comment.dto.res.CommentCreateResponse;
import goorm.athena.domain.comment.dto.res.CommentGetResponse;
import goorm.athena.domain.comment.entity.Comment;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.project.entity.PlanName;
import goorm.athena.domain.project.entity.PlatformPlan;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.service.ProjectService;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@Transactional
class CommentServiceTest extends CommentIntegrationSupport {

    @DisplayName("유저가 해당 프로젝트에 코멘트를 이미 작성했다면 에러를 리턴한다.")
    @Test
    void createComment_Error() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("123", "123", "123", imageGroup);
        Category category = setupCategory("음식");
        BankAccount bankAccount = setupBankAccount(user, "123" ,"123" ,"123", true);
        PlatformPlan platformPlan = platformPlanRepository.findById(1L).get();
        Project project = setupProject(user, category, imageGroup, bankAccount, platformPlan,
                "프로젝2132132131트 제목", "설123213213명", 100000L, 10000L, "!23");
        Comment comment = setupComment(user, project, "123");

        userRepository.save(user);
        categoryRepository.save(category);
        bankAccountRepository.save(bankAccount);
        projectRepository.save(project);
        commentRepository.save(comment);

        Optional<Project> project1 = projectRepository.findById(project.getId());

        // when & then
        assertThatThrownBy(() -> commentService.createComment(project1.get().getId(), user.getId(), "123"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.ALREADY_COMMENTED.getErrorMessage());

    }

    @DisplayName("유저가 해당 프로젝트에 후기를 작성한다.")
    @Test
    void createComment() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("123", "123", "123", imageGroup);
        Category category = setupCategory("음식");
        BankAccount bankAccount = setupBankAccount(user, "123" ,"123" ,"123", true);
        PlatformPlan platformPlan = platformPlanRepository.findById(1L).get();
        Project project = setupProject(user, category, imageGroup, bankAccount, platformPlan,
                "프로젝2132132131트 제목", "설123213213명", 100000L, 10000L, "!23");

        userRepository.save(user);
        categoryRepository.save(category);
        bankAccountRepository.save(bankAccount);
        projectRepository.save(project);

        Optional<Project> project1 = projectRepository.findById(project.getId());

        // when
        CommentCreateResponse response = commentService.createComment(project1.get().getId(), user.getId(), "123123");

        // then
        assertThat(response.userName()).isEqualTo(user.getNickname());
        assertThat(response.content()).isEqualTo("123123");
    }

    @DisplayName("프로젝트 ID로 조회하여 등록된 후기들을 유저 닉네임과 함께 조회한다.")
    @Test
    void getCommentByProject() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        ImageGroup imageGroup2 = setupImageGroup();
        User user = setupUser("123", "123", "123", imageGroup);
        User user2 = setupUser("123", "123", "124", imageGroup2);
        Category category = setupCategory("음식");
        BankAccount bankAccount = setupBankAccount(user, "123" ,"123" ,"123", true);
        PlatformPlan platformPlan = platformPlanRepository.findById(1L).get();
        Project project = setupProject(user, category, imageGroup, bankAccount, platformPlan,
                "프로젝2132132131트 제목", "설123213213명", 100000L, 10000L, "!23");
        Comment comment = setupComment(user, project, "123");
        Comment comment1 = setupComment(user2, project, "12#");

        userRepository.saveAll(List.of(user, user2));
        categoryRepository.save(category);
        bankAccountRepository.save(bankAccount);
        projectRepository.save(project);
        commentRepository.saveAll(List.of(comment, comment1));

        // when
        List<CommentGetResponse> response = commentService.getCommentByProject(project.getId());
        String expectedImageUrl = imageService.getImage(user.getImageGroup().getId());

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
        ImageGroup imageGroup = setupImageGroup();
        ImageGroup imageGroup2 = setupImageGroup();
        User user = setupUser("123", "123", "123", imageGroup);
        User user2 = setupUser("123", "123", "124", imageGroup2);
        Category category = setupCategory("음식");
        BankAccount bankAccount = setupBankAccount(user, "123" ,"123" ,"123", true);
        PlatformPlan platformPlan = platformPlanRepository.findById(1L).get();
        Project project = setupProject(user, category, imageGroup, bankAccount, platformPlan,
                "프로젝2132132131트 제목", "설123213213명", 100000L, 10000L, "!23");
        Comment comment = setupComment(user, project, "123");
        Comment comment1 = setupComment(user2, project, "12#");

        userRepository.saveAll(List.of(user, user2));
        categoryRepository.save(category);
        bankAccountRepository.save(bankAccount);
        projectRepository.save(project);
        commentRepository.saveAll(List.of(comment, comment1));

        // when
        List<CommentGetResponse> response = commentService.getCommentByUser(user.getId());
        String expectedImageUrl = imageService.getImage(user.getImageGroup().getId());

        // then
        assertThat(response).hasSize(1);

        CommentGetResponse commentResponse = response.get(0);
        assertThat(commentResponse.content()).isEqualTo("123");
        assertThat(commentResponse.userName()).isEqualTo(user.getNickname());

        assertThat(commentResponse.imageUrl()).isEqualTo(expectedImageUrl);
    }
}