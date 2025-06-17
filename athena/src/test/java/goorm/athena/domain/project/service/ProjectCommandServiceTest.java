package goorm.athena.domain.project.service;

import goorm.athena.domain.image.entity.Image;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.product.dto.req.ProductRequest;
import goorm.athena.domain.product.dto.res.ProductResponse;
import goorm.athena.domain.project.dto.req.ProjectCreateRequest;
import goorm.athena.domain.project.dto.req.ProjectUpdateRequest;
import goorm.athena.domain.project.dto.res.ProjectIdResponse;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.util.ProjectIntegrationTestSupport;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class ProjectCommandServiceTest extends ProjectIntegrationTestSupport {

    @DisplayName("프로젝트 생성 시, 받아오는 프로젝트 정보들과 마크다운 첨부 이미지가 정상적으로 저장된다.")
    @Test
    void createProjectWithMarkdownImages(){
        // given
        ProjectCreateRequest request = createProjectRequest("테스트 프로젝트", "테스트 설명",
                "![image](test1.jpg)", LocalDateTime.now().plusDays(8));
        MultipartFile markdownFile1 = createMockFile("test1.jpg", "image/jpeg");

        // when
        ProjectIdResponse response = projectCommandService.createProject(request, List.of(markdownFile1));

        // then
        Project project = projectQueryService.getById(response.projectId());
        List<Image> markdownImages = imageQueryService.getAllImages(project.getImageGroup()).stream()
                .filter(image -> image.getImageIndex() == 0)
                .toList();
        List<String> imageUrls = imageQueryService.getImageUrls(markdownImages);

        assertThat(project.getTitle()).isEqualTo(request.title());
        assertThat(project.getDescription()).isEqualTo(request.description());
        assertThat(
                imageUrls.stream()
                        .allMatch(url -> project.getContentMarkdown().contains(url))
        ).isTrue();
        assertThat(markdownImages).isNotEmpty();
    }

    @DisplayName("프로젝트 생성 시, 마크다운 첨부 이미지가 존재하지 않아도 프로젝트가 정상적으로 생성된다.")
    @Test
    void createProjectWithoutMarkdownImages(){
        // given
        ProjectCreateRequest request = createProjectRequest("테스트 프로젝트", "테스트 설명",
                "이미지가 없는 마크다운입니다.", LocalDateTime.now().plusDays(8));

        // when
        ProjectIdResponse response = projectCommandService.createProject(request, List.of());

        // then
        Project project = projectQueryService.getById(response.projectId());
        List<Image> markdownImages = imageQueryService.getAllImages(project.getImageGroup()).stream()
                .filter(image -> image.getImageIndex() == 0)
                .toList();

        assertThat(project.getTitle()).isEqualTo(request.title());
        assertThat(project.getDescription()).isEqualTo(request.description());
        assertThat(project.getContentMarkdown()).isEqualTo(request.contentMarkdown());
        assertThat(markdownImages).isEmpty();
    }

//    @DisplayName("상품 리스트가 비어있는 경우 예외가 발생한다.")
//    @Test
//    void createProjectWithEmptyProductList(){
//        // given
//
//        // when
//
//        // then
//
//    }

    @DisplayName("제목 길이가 25자를 초과하면 예외가 발생한다.")
    @Test
    void createProjectWithTitleTooLong(){
        // given
        String longTitle = "a".repeat(26);
        ProjectCreateRequest request = createProjectRequest(longTitle, "테스트 설명",
                "이미지가 없는 마크다운입니다.", LocalDateTime.now().plusDays(8));

        // when, then
        assertThatThrownBy(() -> projectCommandService.createProject(request, List.of()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TITLE_FORMAT);
    }

    @DisplayName("설명 길이가 50자를 초과하면 예외가 발생한다.")
    @Test
    void createProjectWithDescriptionTooLong(){
        // given
        String longDescription = "a".repeat(51);
        ProjectCreateRequest request = createProjectRequest("테스트 제목", longDescription,
                "이미지가 없는 마크다운입니다.", LocalDateTime.now().plusDays(8));

        // when, then
        assertThatThrownBy(() -> projectCommandService.createProject(request, List.of()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_DESCRIPTION_FORMAT);
    }

    @DisplayName("시작일이 현재 날짜 +7일보다 이전일 경우, 예외가 발생한다.")
    @Test
    void createProjectWithInvalidStartDate(){
        // given
        LocalDateTime invalidStartDate = LocalDateTime.now().plusDays(6);
        ProjectCreateRequest request = createProjectRequest("테스트 제목", "테스트 설명",
                "이미지가 없는 마크다운입니다.", invalidStartDate);

        // when, then
        assertThatThrownBy(() -> projectCommandService.createProject(request, List.of()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_STARTDATE);
    }

    @DisplayName("프로젝트 수정 시, 받아온 프로젝트 정보들과 이미지에 맞게 프로젝트 관련 필드들이 변경된다.")
    @Test
    void updateProject(){
        // given
        ProjectCreateRequest request = createProjectRequest("테스트 프로젝트", "테스트 설명", "![image](test1.jpg)", LocalDateTime.now().plusDays(8));
        MultipartFile markdownFile1 = createMockFile("test1.jpg", "image/jpeg");
        ProjectIdResponse response = projectCommandService.createProject(request, List.of(markdownFile1));
        Long projectId = response.projectId();

        ProductRequest updateProduct = new ProductRequest(null, null, null, 50L, null);
        ProjectUpdateRequest updateRequest = new ProjectUpdateRequest(
                2L, 1L, "수정된 제목", "수정된 설명", 200000L,
                "![newImage](new.jpg)", LocalDateTime.now().plusDays(20), LocalDateTime.now().plusDays(50), LocalDateTime.now().plusDays(80),
                List.of(updateProduct));
        MultipartFile newThumbnail = createMockFile("thumbnail.jpg", "image/jpeg");
        MultipartFile newMarkdownImage = createMockFile("new.jpg", "image/jpeg");

        // when
        projectCommandService.updateProject(projectId, updateRequest, List.of(newThumbnail), List.of(newMarkdownImage));

        // then
        Project updatedProject = projectQueryService.getById(projectId);
        List<Image> markdownImages = imageQueryService.getAllImages(updatedProject.getImageGroup()).stream()
                .filter(image -> image.getImageIndex() == 0)
                .toList();
        List<String> imageUrls = imageQueryService.getImageUrls(markdownImages);

        assertThat(updatedProject.getTitle()).isEqualTo(updateRequest.title());
        assertThat(updatedProject.getDescription()).isEqualTo(updateRequest.description());
        assertThat(updatedProject.getGoalAmount()).isEqualTo(updateRequest.goalAmount());
        assertThat(
                imageUrls.stream()
                        .allMatch(url -> updatedProject.getContentMarkdown().contains(url))
        ).isTrue();
    }

    @DisplayName("프로젝트 삭제 시, 이미지/상품/프로젝트/이미지그룹이 모두 삭제된다.")
    @Test
    void deleteProject(){
        // given
        ProjectCreateRequest request = createProjectRequest("테스트 프로젝트", "테스트 설명", "![image](test1.jpg)", LocalDateTime.now().plusDays(8));
        MultipartFile markdownFile1 = createMockFile("test1.jpg", "image/jpeg");
        ProjectIdResponse response = projectCommandService.createProject(request, List.of(markdownFile1));

        Long projectId = response.projectId();
        Project project = projectQueryService.getById(response.projectId());
        ImageGroup imageGroup = project.getImageGroup();

        // when
        projectCommandService.deleteProject(projectId);

        // then
        List<Image> images = imageQueryService.getAllImages(imageGroup);
        List<ProductResponse> products = productQueryService.getAllProducts(project);

        assertThat(images).isEmpty();
        assertThat(products).isEmpty();
        assertThatThrownBy(() -> projectQueryService.getById(projectId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);
        assertThatThrownBy(() -> imageGroupQueryService.getById(imageGroup.getId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_GROUP_NOT_FOUND);

    }

    @DisplayName("프로젝트 삭제 시, 존재하지 않는 프로젝트 ID를 삭제하는 경우 예외가 발생한다.")
    @Test
    void deleteInvalidProject(){
        // given
        Long invalidProjectId = 100000L;

        // when, then
        assertThatThrownBy(() -> projectCommandService.deleteProject(invalidProjectId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);
    }
}