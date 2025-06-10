package goorm.athena.domain.comment.controller;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.comment.CommentControllerIntegrationSupport;
import goorm.athena.domain.comment.dto.req.CommentCreateRequest;
import goorm.athena.domain.comment.dto.res.CommentCreateResponse;
import goorm.athena.domain.comment.dto.res.CommentGetResponse;
import goorm.athena.domain.comment.entity.Comment;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.project.entity.PlatformPlan;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.user.dto.request.UserUpdateRequest;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.userCoupon.entity.QUserCoupon;
import goorm.athena.global.jwt.util.LoginUserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommentControllerImplTest extends CommentControllerIntegrationSupport {

    @DisplayName("로그인 한 사용자가 특정 프로젝트에 코멘트를 작성한다.")
    @Test
    void createComment() {
        // given
        ImageGroup userImageGroup = setupUserImageGroup();
        ImageGroup projectImageGroup = setupProjectImageGroup();
        User user = setupUser("123", "123", "123", userImageGroup);
        Category category = setupCategory("음식");
        BankAccount bankAccount = setupBankAccount(user, "123" ,"123" ,"123", true);

        userRepository.save(user);
        categoryRepository.save(category);
        bankAccountRepository.save(bankAccount);

        PlatformPlan platformPlan = platformPlanRepository.findById(1L).get();
        Project project = setupProject(user, category, projectImageGroup, bankAccount, platformPlan,
                "프로젝2132132131트 제목", "설123213213명", 100000L, 10000L, "!23'");
        projectRepository.save(project);


        String content = "123";
        LoginUserRequest loginUserRequest = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);
        CommentCreateRequest request = new CommentCreateRequest(project.getId(), content);

        // when
        ResponseEntity<CommentCreateResponse> response = controller.createComment(loginUserRequest, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(content).isEqualTo(response.getBody().content()); // 결과 검증
    }

    @DisplayName("특정 프로젝트 ID의 코멘트 목록들을 조회한다. ")
    @Test
    void getComment() throws IOException {
        ImageGroup userImageGroup = setupUserImageGroup();
        ImageGroup userImageGroup2 = setupUserImageGroup();
        ImageGroup projectImageGroup = setupProjectImageGroup();
        User user = setupUser("123", "123", "123", userImageGroup);
        User user2 = setupUser("123", "123", "124", userImageGroup2);
        Category category = setupCategory("음식");
        BankAccount bankAccount = setupBankAccount(user, "123" ,"123" ,"123", true);

        userRepository.saveAll(List.of(user, user2));
        categoryRepository.save(category);
        bankAccountRepository.save(bankAccount);

        UserUpdateRequest request = new UserUpdateRequest("newNick", "소개글", "https://link.com");

        PlatformPlan platformPlan = platformPlanRepository.findById(1L).get();

        Project project = setupProject(user, category, projectImageGroup, bankAccount, platformPlan,
                "프로젝2132132131트 제목", "설123213213명", 100000L, 10000L, "!23");
        projectRepository.save(project);

        // MultipartFile 생성]
        BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", os); // WebP 포맷 지원 라이브러리 필요

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                new ByteArrayInputStream(os.toByteArray())
        );

        userCommandService.updateUser(user.getId(), request, multipartFile);

        User updatedUser = userRepository.findById(user.getId()).get();
        User updatedUser2 = userRepository.findById(user2.getId()).get();

        Comment comment = setupComment(user, project, "123");
        Comment comment1 = setupComment(user2, project, "123");
        commentRepository.saveAll(List.of(comment, comment1));

        CommentGetResponse response1 = new CommentGetResponse(
                1L, "123" ,"프로젝2132132131트 제목" ,"123", LocalDateTime.now(), project.getId(), imageService.getImage(updatedUser.getImageGroup().getId())
        );

        CommentGetResponse response2 = new CommentGetResponse(
                2L, "124" ,"프로젝2132132131트 제목" ,"123", LocalDateTime.now(), project.getId(), imageService.getImage(updatedUser2.getImageGroup().getId())
        );

        List<CommentGetResponse> expectedResponse = List.of(response1, response2);

        // when
        ResponseEntity<List<CommentGetResponse>> responses = controller.getComment(project.getId());

        // then
        assertThat(responses.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(expectedResponse.get(0).imageUrl()).isEqualTo(responses.getBody().getFirst().imageUrl());
        assertThat(expectedResponse.get(1).content()).isEqualTo(responses.getBody().get(1).content());
    }
}