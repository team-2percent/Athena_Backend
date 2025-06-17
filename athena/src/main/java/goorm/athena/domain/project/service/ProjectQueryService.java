package goorm.athena.domain.project.service;

import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.category.service.CategoryService;
import goorm.athena.domain.image.entity.Image;
import goorm.athena.domain.image.service.ImageQueryService;
import goorm.athena.domain.product.dto.res.ProductResponse;
import goorm.athena.domain.product.service.ProductQueryService;
import goorm.athena.domain.project.dto.cursor.*;
import goorm.athena.domain.project.dto.req.*;
import goorm.athena.domain.project.dto.res.*;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.entity.SortTypeDeadline;
import goorm.athena.domain.project.entity.SortTypeLatest;
import goorm.athena.domain.project.mapper.ProjectMapper;
import goorm.athena.domain.project.repository.ProjectRepository;
import goorm.athena.domain.project.repository.query.ProjectFilterQueryRepository;
import goorm.athena.domain.project.repository.query.ProjectQueryRepository;
import goorm.athena.domain.project.repository.query.ProjectSearchQueryRepository;
import goorm.athena.domain.project.util.ProjectQueryType;
import goorm.athena.domain.user.dto.response.UserDetailResponse;
import goorm.athena.domain.user.mapper.UserMapper;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ProjectQueryService {
    private final ProjectRepository projectRepository;
    private final ImageQueryService imageQueryService;
    private final ProductQueryService productQueryService;
    private final ProjectQueryRepository projectQueryRepository;
    private final ProjectFilterQueryRepository projectFilterQueryRepository;
    private final ProjectSearchQueryRepository projectSearchQueryRepository;
    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;

    // 상세 페이지 조회
    public ProjectDetailResponse getProjectDetail(Long projectId) {
        ProjectDetailDto dto = projectQueryRepository.getProjectDetail(projectId);

        UserDetailResponse userDetailResponse = userMapper.toDetailResponse(dto.seller());
        List<String> imageUrls = imageQueryService.getImageUrls(dto.images());
        List<ProductResponse> products = productQueryService.getAllProducts(dto.project());

        return projectMapper.toDetailDto(dto.project(), dto.category(), imageUrls, userDetailResponse, products);
    }

    // 메인 페이지 조회
    public List<ProjectAllResponse> getProjects() {
        List<Project> projects = projectRepository.findTopNWithImageGroupByOrderByViewsDesc(20);

        AtomicInteger rank = new AtomicInteger(1);
        return projects.stream()
                .map(project -> {
                    String imageUrl = imageQueryService.getImage(project.getImageGroup().getId());
                    int currentRank = rank.getAndIncrement();
                    return ProjectAllResponse.from(project, imageUrl, currentRank);
                })
                .collect(Collectors.toList());
    }

    // ToDo 커서 기반 페이징 조회 메서드 통합 중
    // ToDo 각 case 안에서 requestDto를 다운캐스팅하여 사용하고 있지만, 추후 일반화 할 예정
    public <T extends ProjectQueryBaseRequest> ProjectCursorBaseResponse getProjectsWithCursor(
            ProjectQueryType queryType,
            Optional<ProjectCursorRequest<?>> cursorRequest,
            T requestDto // queryType에 따라 requestDto 타입이 달라지도록 추상화
    ) {
        switch (queryType) {
            case LATEST:
                if (requestDto instanceof ProjectQueryLatestRequest latestRequest) {
                    return getProjectsByNew(latestRequest.lastStartAt(), latestRequest.lastProjectId(),
                            latestRequest.pageSize());
                }
                // ToDo 팔로워 수 기준 조회 기능 추가 예정
                // case POPULAR:
                // if (requestDto instanceof ProjectQueryPopularRequest popularRequest) {
                // return getProjectsByPopular(popularRequest.lastStartAt(),
                // popularRequest.lastProjectId(),
                // popularRequest.pageSize());
                // }
            case CATEGORY:
                if (requestDto instanceof ProjectQueryCategoryRequest categoryRequest) {
                    if (cursorRequest.isEmpty()) {
                        throw new IllegalArgumentException("cursorRequest는 null일 수 없습니다.");
                    }
                    return getProjectsByCategory(cursorRequest.get(), categoryRequest.categoryId(),
                            categoryRequest.sortType());
                }
            case DEADLINE:
                if (requestDto instanceof ProjectQueryDeadlineRequest deadlineRequest) {
                    return getProjectsByDeadLine(deadlineRequest.lastStartAt(), deadlineRequest.sortTypeDeadline(),
                            deadlineRequest.lastProjectId(), deadlineRequest.pageSize());
                }
                // ToDo 성공률 기준 조회 기능 추가 예정
                // case SUCCESS_RATE:
                // if (requestDto instanceof ProjectQuerySuccessRateRequest successRateRequest)
                // {
                // return getProjectsBySuccessRate(cursorRequest);
                // }
            case SEARCH:
                if (requestDto instanceof ProjectQuerySearchRequest searchRequest) {
                    if (cursorRequest.isEmpty()) {
                        throw new IllegalArgumentException("cursorRequest는 null일 수 없습니다.");
                    }
                    return searchProjects(cursorRequest.get(), searchRequest.searchTerms(), searchRequest.sortType());
                }
        }
        return null;
    }

    // 최신 프로젝트 조회 (커서 기반 페이징)
    public ProjectRecentCursorResponse getProjectsByNew(LocalDateTime lastStartAt, Long lastProjectId, int pageSize) {
        ProjectCursorRequest<LocalDateTime> request = new ProjectCursorRequest<>(lastStartAt, lastProjectId, pageSize);
        return projectQueryRepository.getProjectsByNew(request);
    }

    // 카테고리별 프로젝트 조회 (커서 기반 페이징)
    public ProjectCategoryCursorResponse getProjectsByCategory(ProjectCursorRequest<?> request, Long categoryId,
                                                               SortTypeLatest sortType) {

        return projectFilterQueryRepository.getProjectsByCategory(request, categoryId, sortType);
    }

    // 마감 기한별 프로젝트 조회 (커서 기반 페이징)
    public ProjectDeadlineCursorResponse getProjectsByDeadLine(LocalDateTime lastStartAt,
                                                               SortTypeDeadline sortTypeDeadline, Long lastProjectId, int pageSize) {
        ProjectCursorRequest<LocalDateTime> request = new ProjectCursorRequest<>(lastStartAt, lastProjectId, pageSize);
        return projectFilterQueryRepository.getProjectsByDeadline(request, sortTypeDeadline);
    }

    // 검색 프로젝트 조회 (커서 기반 페이징)
    public ProjectSearchCursorResponse searchProjects(ProjectCursorRequest<?> request, String searchTerms,
                                                      SortTypeLatest sortType) {
        return projectSearchQueryRepository.searchProjects(request, searchTerms, sortType);
    }

    public List<ProjectByPlanGetResponse> getTopViewByPlan() {
        List<Project> projects = projectRepository.findTop5ProjectsGroupedByPlatformPlan();

        // 요금제 이름(planName) 별로 그룹핑
        Map<String, List<Project>> groupedByPlan = projects.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getPlatformPlan().getName().name(), // PlanName -> String
                        LinkedHashMap::new,
                        Collectors.toList()));

        // 각 그룹을 ProjectByPlanGetResponse 로 매핑
        return groupedByPlan.entrySet().stream()
                .map(entry -> {
                    String planName = entry.getKey();
                    List<ProjectTopViewResponse> items = entry.getValue().stream()
                            .map(project -> {
                                String imageUrl = imageQueryService.getImage(project.getImageGroup().getId());
                                System.out.println(imageUrl);
                                return projectMapper.toTopViewResponse(project, imageUrl);
                            })
                            .toList();
                    return new ProjectByPlanGetResponse(planName, items);
                })
                .toList();
    }

    // ToDo 아래 코드에서 TOP 5 뽑는 부분들은 `findTopNWithImageGroupByOrderByViewsDesc` 메서드로
    // 대체할 예정
    public ProjectCategoryTopResponseWrapper getTopView() {
        List<Project> projects = projectRepository.findTopViewedProjectsByCategory();

        // 전체 조회수 기준 Top 5 (카테고리 상관없이)
        List<ProjectTopViewResponse> globalTop5 = projects.stream()
                .sorted(Comparator.comparingLong(Project::getViews).reversed())
                .limit(5)
                .map(project -> {
                    String imageUrl = imageQueryService.getImage(project.getImageGroup().getId());
                    return projectMapper.toTopViewResponse(project, imageUrl);
                })
                .toList();

        // 카테고리별 Top 5
        Map<Category, List<Project>> groupedByCategory = projects.stream()
                .collect(Collectors.groupingBy(Project::getCategory, LinkedHashMap::new,
                        Collectors.toList()));

        List<ProjectCategoryTopViewResponse> categoryTopViews = groupedByCategory.entrySet().stream()
                .map(entry -> {
                    Category category = entry.getKey();
                    List<ProjectTopViewResponse> topViewResponses = entry.getValue().stream()
                            .map(project -> {
                                String imageUrl = imageQueryService.getImage(project.getImageGroup().getId());
                                return projectMapper.toTopViewResponse(project, imageUrl);
                            })
                            .toList();
                    return projectMapper.toCategoryTopView(category, topViewResponses);
                })
                .toList();

        return new ProjectCategoryTopResponseWrapper(globalTop5, categoryTopViews);
    }

    // 후원 기간 종료, 목표금액 달성, 중복 정산 제외 조건이 충족해야함
    public List<Project> getEligibleProjects(LocalDate baseDate) {
        LocalDateTime endAt = baseDate.plusDays(1).atStartOfDay();
        return projectQueryRepository.findProjectsWithUnsettledOrders(endAt);
    }

    // Get Project
    public Project getById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
    }

}
