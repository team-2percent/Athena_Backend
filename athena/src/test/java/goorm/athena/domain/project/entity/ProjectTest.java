package goorm.athena.domain.project.entity;

import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.project.util.ProjectIntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ProjectTest extends ProjectIntegrationTestSupport {

    @DisplayName("프로젝트가 처음 생성되는 경우, 승인 상태는 PENDING이고, 프로젝트 상태는 QUEUED이다.")
    @Test
    void initializeProjectStatus(){
        // given
        Project project = setupProject("테스트 프로젝트", "설명", 10000L, 0L, "마크다운 내용");

        // when, then
        assertThat(project.getIsApproved()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(project.getStatus()).isEqualTo(Status.QUEUED);
    }

    @DisplayName("프로젝트 승인 상태가 APPROVED로 변경될 경우, 프로젝트 상태가 ACTIVE로 바뀐다.")
    @Test
    void changeProjectStatusToActive(){
        // given
        Project project = setupProject("테스트 프로젝트", "설명", 10000L, 0L, "마크다운 내용");

        // when
        project.setApprovalStatus(true);

        // then
        assertThat(project.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(project.getIsApproved()).isEqualTo(ApprovalStatus.APPROVED);
    }

    @DisplayName("프로젝트 승인 상태가 REJECTED로 변경될 경우, 프로젝트 상태가 CANCELLED로 변경된다.")
    @Test
    void changeProjectStatusToCancelled(){
        // given
        Project project = setupProject("테스트 프로젝트", "설명", 10000L, 0L, "마크다운 내용");

        // when
        project.setApprovalStatus(false);

        // then
        assertThat(project.getStatus()).isEqualTo(Status.CANCELLED);
        assertThat(project.getIsApproved()).isEqualTo(ApprovalStatus.REJECTED);
    }

    @DisplayName("받아온 입력 값에 맞춰 프로젝트 정보가 수정된다.")
    @Test
    void updateProjectInfo(){
        // given
        Project project = setupProject("테스트 프로젝트", "설명", 10000L, 0L, "마크다운 내용");
        Category newCategory = categoryService.getCategoryById(2L);
        LocalDateTime newEndDate = LocalDateTime.now().plusDays(30);

        // when
        project.update(newCategory, project.getBankAccount(), "수정된 프로젝트", "수정된 설명", 100000L,
                "수정된 마크다운", project.getStartAt(), newEndDate, project.getShippedAt());

        // then
        assertThat(project.getEndAt()).isEqualTo(newEndDate);
        assertThat(project.getCategory().getId()).isEqualTo(newCategory.getId());
        assertThat(project.getTitle()).isEqualTo("수정된 프로젝트");
        assertThat(project.getDescription()).isEqualTo("수정된 설명");
        assertThat(project.getGoalAmount()).isEqualTo(100000L);
        assertThat(project.getContentMarkdown()).isEqualTo("수정된 마크다운");
    }

    @DisplayName("받아온 금액이 totalAmount에 누적 값으로 반영된다.")
    @Test
    void increaseTotalAmount(){
        // given
        Project project = setupProject("테스트 프로젝트", "설명", 10000L, 1000L, "마크다운 내용");

        // when
        project.increasePrice(1000L);

        // then
        assertThat(project.getTotalAmount()).isEqualTo(2000L);
    }
}