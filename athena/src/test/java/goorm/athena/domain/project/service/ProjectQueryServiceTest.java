package goorm.athena.domain.project.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

import goorm.athena.domain.project.util.ProjectIntegrationTestSupport;
import goorm.athena.domain.category.repository.CategoryRepository;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.repository.ImageGroupRepository;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.bankaccount.repository.BankAccountRepository;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.repository.PlatformPlanRepository;
import goorm.athena.domain.project.entity.PlanName;
import goorm.athena.domain.category.util.DefaultCategories;
import goorm.athena.domain.project.util.ProjectQueryType;
import goorm.athena.domain.project.dto.cursor.ProjectRecentCursorResponse;
import goorm.athena.domain.project.dto.req.ProjectQueryLatestRequest;
import goorm.athena.domain.project.dto.res.ProjectCategoryTopResponseWrapper;

/*
 * 프로젝트 서비스 중 조회 관련 메서드를 테스트합니다.
 */
public class ProjectQueryServiceTest extends ProjectIntegrationTestSupport {

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private ImageGroupRepository imageGroupRepository;

  @Autowired
  private PlatformPlanRepository platformPlanRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private BankAccountRepository bankAccountRepository;

  private Project createProjectWithDependencies(String categoryName, PlanName planName, LocalDateTime startAt,
      LocalDateTime endAt, Long views) {
    categoryName = categoryName == null ? "기타" : categoryName;
    planName = planName == null ? PlanName.BASIC : planName;
    startAt = startAt == null ? LocalDateTime.now() : startAt;
    endAt = endAt == null ? LocalDateTime.now().plusDays(30) : endAt;
    views = views == null ? 0L : views;

    ImageGroup userImageGroup = ImageGroup.builder()
        .type(Type.USER)
        .build();
    imageGroupRepository.save(userImageGroup);

    ImageGroup projectImageGroup = ImageGroup.builder()
        .type(Type.PROJECT)
        .build();
    imageGroupRepository.save(projectImageGroup);

    User user = User.createFullUser(
        userImageGroup,
        "test@test.com",
        "test1234",
        "테스트 사용자",
        Role.ROLE_USER,
        "테스트 사용자 소개",
        "https://test.com");
    userRepository.save(user);

    BankAccount bankAccount = BankAccount.builder()
        .user(user)
        .accountNumber("1234567890")
        .accountHolder(user.getNickname())
        .bankName("테스트 은행")
        .isDefault(true)
        .build();
    bankAccountRepository.save(bankAccount);

    Project project = Project.builder()
        .seller(user)
        .category(categoryRepository.findByCategoryName(categoryName).get())
        .imageGroup(projectImageGroup)
        .bankAccount(bankAccount)
        .platformPlan(platformPlanRepository.findByName(planName))
        .title("테스트 프로젝트")
        .description("테스트 프로젝트 설명")
        .goalAmount(1000000L)
        .totalAmount(0L)
        .contentMarkdown("테스트 프로젝트 소개")
        .startAt(startAt)
        .endAt(endAt)
        .shippedAt(null)
        .views(views)
        .build();
    project.setApprovalStatus(true);
    return projectRepository.save(project);
  }

  @DisplayName("프로젝트 전체 조회에서 페이지 1을 조회합니다.")
  @Test
  void testGetProjectsPage1() {
    // given
    DefaultCategories.VALUES.forEach(categoryName -> {
      for (int i = 0; i < 6; i++) {
        createProjectWithDependencies(categoryName, PlanName.BASIC, LocalDateTime.now(),
            LocalDateTime.now().plusDays(30), 0L);
      }
    });

    // when
    ProjectRecentCursorResponse result = (ProjectRecentCursorResponse) projectQueryService.getProjectsWithCursor(
        ProjectQueryType.LATEST, Optional.empty(),
        new ProjectQueryLatestRequest(LocalDateTime.now(), null, 20));

    // then
    assertThat(result.content().size()).isGreaterThanOrEqualTo(20);
  }

  @DisplayName("프로젝트 전체 조회에서 조회수 기준 TOP 5 항목을 조회합니다.")
  @Test
  void testGetTop5ProjectsByViews() {
    // given
    DefaultCategories.VALUES.forEach(categoryName -> {
      Random random = new Random();
      for (int i = 0; i < 6; i++) {
        createProjectWithDependencies(categoryName, PlanName.BASIC, LocalDateTime.now(),
            LocalDateTime.now().plusDays(30), random.nextLong(1000));
      }
    });

    // when
    ProjectCategoryTopResponseWrapper result = projectQueryService.getTopView();

    // then
    assertThat(result.allTopView().size()).isEqualTo(5);
  }
  
  @DisplayName("프로젝트 카테고리 조회에서 조회수 기준 TOP5 항목을 조회합니다.")
  @Test
  void testGetTop5ProjectsByCategory() {
    // given
    // data.sql에 있는 프로젝트 중 조회수가 높은 순으로 5개를 조회합니다.

    // when
    ProjectCategoryTopResponseWrapper result = projectQueryService.getTopView();

    // then
    assertThat(result.allTopView().size()).isEqualTo(5);
  }
}
