package goorm.athena.domain.project.service;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.bankaccount.service.BankAccountQueryService;
import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.category.service.CategoryService;
import goorm.athena.domain.image.entity.Image;
import goorm.athena.domain.image.service.ImageService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.product.dto.req.ProductRequest;
import goorm.athena.domain.product.dto.res.ProductResponse;
import goorm.athena.domain.product.service.ProductService;
import goorm.athena.domain.project.dto.cursor.*;
import goorm.athena.domain.project.dto.req.ProjectCreateRequest;
import goorm.athena.domain.project.dto.req.ProjectQueryBaseRequest;
import goorm.athena.domain.project.dto.req.ProjectQueryLatestRequest;
import goorm.athena.domain.project.dto.req.ProjectQueryCategoryRequest;
import goorm.athena.domain.project.dto.req.ProjectQueryDeadlineRequest;
import goorm.athena.domain.project.dto.req.ProjectQuerySearchRequest;
import goorm.athena.domain.project.dto.req.ProjectUpdateRequest;
import goorm.athena.domain.project.dto.res.ProjectIdResponse;
import goorm.athena.domain.project.dto.req.ProjectCursorRequest;
import goorm.athena.domain.project.dto.res.*;
import goorm.athena.domain.project.entity.*;
import goorm.athena.domain.project.mapper.ProjectMapper;
import goorm.athena.domain.project.repository.PlatformPlanRepository;
import goorm.athena.domain.project.repository.query.ProjectFilterQueryRepository;
import goorm.athena.domain.project.repository.query.ProjectQueryRepository;
import goorm.athena.domain.project.repository.ProjectRepository;
import goorm.athena.domain.project.repository.query.ProjectSearchQueryRepository;
import goorm.athena.domain.user.dto.response.UserDetailResponse;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.mapper.UserMapper;
import goorm.athena.domain.user.service.UserQueryService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import goorm.athena.domain.project.util.ProjectQueryType;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ImageGroupService imageGroupService;
    private final ImageService imageService;
    private final UserQueryService userQueryService;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final BankAccountQueryService bankAccountQueryService;
    private final ProjectQueryRepository projectQueryRepository;
    private final ProjectFilterQueryRepository projectFilterQueryRepository;
    private final ProjectSearchQueryRepository projectSearchQueryRepository;
    private final MarkdownParser markdownParser;
    private final PlatformPlanRepository platformPlanRepository;

    /**
     * [프로젝트 등록 Method]
     */
    @Transactional
    public ProjectIdResponse createProject(ProjectCreateRequest request, List<MultipartFile> markdownFiles) {
        ImageGroup imageGroup = imageGroupService.getById(request.imageGroupId());
        User seller = userQueryService.getUser(request.sellerId());
        Category category = categoryService.getCategoryById(request.categoryId());
        BankAccount bankAccount = bankAccountQueryService.getAccount(request.sellerId(), request.bankAccountId());
        PlanName planName = PlanName.valueOf(request.platformPlan());
        PlatformPlan platformPlan = platformPlanRepository.findByName(planName);

        validateProduct(request); // 프로젝트 등록 시 검증

        // 마크다운에 로컬 이미지가 삽입된 경우 이를 이미지 URL로 치환
        String convertedMarkdown = convertMarkdownIfNeeded(request.contentMarkdown(), markdownFiles, imageGroup);

        Project project = ProjectMapper.toEntity(request, seller, imageGroup, category, bankAccount, platformPlan,
                convertedMarkdown); // 새 프로젝트 생성
        Project savedProject = projectRepository.save(project); // 프로젝트 저장

        createProducts(request.products(), project); // 상품 등록 요청 처리
        // 상품 생성 예외 처리 추가적으로 있을지 고려

        return ProjectMapper.toCreateDto(savedProject);
    }

    // 상품 리스트 생성
    private void createProducts(List<ProductRequest> requests, Project project) {
        if (!CollectionUtils.isEmpty(requests)) {
            productService.saveProducts(requests, project);
        } else {
            throw new CustomException(ErrorCode.PRODUCT_IS_EMPTY);
        }
    }

    // 프로젝트 등록 검증
    private void validateProduct(ProjectCreateRequest request) {
        if (request.title().length() > 25) {
            throw new CustomException(ErrorCode.INVALID_TITLE_FORMAT);
        }

        if (request.description().length() > 50) {
            throw new CustomException(ErrorCode.INVALID_DESCRIPTION_FORMAT);
        }

        LocalDate todayPlus7 = LocalDate.now().plusDays(7);
        LocalDate startDate = request.startAt().toLocalDate();

        if (startDate.isBefore(todayPlus7)) {
            throw new CustomException(ErrorCode.INVALID_STARTDATE);
        }
    }

    // 마크 다운 변환
    private String convertMarkdownIfNeeded(String markdown, List<MultipartFile> markdownFiles, ImageGroup imageGroup) {
        if (CollectionUtils.isEmpty(markdownFiles)) {
            return markdown;
        }

        try {
            return getConvertedMarkdown(markdownFiles, imageGroup, markdown); // 기존 마크다운 -> URL 치환 마크다운
        } catch (IOException e) {
            throw new CustomException(ErrorCode.IMAGES_UPLOAD_FAILED);
        }
    }

    private String getConvertedMarkdown(List<MultipartFile> markdownFiles, ImageGroup imageGroup, String markdown)
            throws IOException {
        List<String> imagePaths = markdownParser.extractImagePaths(markdown); // 마크다운 내 url 추출
        List<String> realUrls = imageService.uploadMarkdownImages(markdownFiles, imageGroup); // 이미지 저장 및 이미지 서버 url 반환

        return markdownParser.replaceMarkdown(markdown, imagePaths, realUrls);
    }

    /**
     * [프로젝트 수정 Method]
     */
    @Transactional
    public void updateProject(Long projectId, ProjectUpdateRequest request, List<MultipartFile> files,
            List<MultipartFile> markdownFiles) {
        Project project = getById(projectId);
        Category category = categoryService.getCategoryById(request.categoryId());
        BankAccount bankAccount = bankAccountQueryService.getPrimaryAccount(request.bankAccountId());

        // 마크다운 이미지, 대표 이미지 PUT 작업을 위해서 이미지 미리 전체 삭제
        imageService.deleteImages(project.getImageGroup());

        // 마크다운에 로컬 이미지가 삽입된 경우 이를 이미지 URL로 치환
        String convertedMarkdown = convertMarkdownIfNeeded(request.contentMarkdown(), markdownFiles,
                project.getImageGroup());

        if (!CollectionUtils.isEmpty(files)) {
            imageService.uploadImages(files, project.getImageGroup());
        } else {
            throw new CustomException(ErrorCode.IMAGE_IS_REQUIRED);
        }

        // 상품 업데이트 (가격만)
        productService.updateProducts(request.products(), project);

        project.update(
                category,
                bankAccount,
                request.title(),
                request.description(),
                request.goalAmount(),
                convertedMarkdown,
                request.startAt(),
                request.endAt(),
                request.shippedAt());

        // 상품 및 이미지 업데이트 (PUT)
        deleteProducts(project);
        createProducts(request.products(), project);
    }

    /**
     * [프로젝트 삭제 Method]
     */
    @Transactional
    public void deleteProject(Long projectId) {
        Project project = getById(projectId);
        ImageGroup imageGroup = project.getImageGroup();

        imageService.deleteImages(imageGroup); // 이미지 삭제
        deleteProducts(project); // 상품 -> 옵션 삭제
        projectRepository.delete(project); // 프로젝트 삭제
        imageGroupService.deleteImageGroup(imageGroup); // 이미지 그룹 삭제

    }

    // 상품 리스트 삭제
    private void deleteProducts(Project project) {
        productService.deleteAllByProject(project);
    }

    /**
     * [GET API 관련 Method]
     */

    // 상세 페이지 조회
    @Transactional(readOnly = true)
    public ProjectDetailResponse getProjectDetail(Long projectId) {
        Project project = getById(projectId);

        Category category = categoryService.getCategoryById(project.getCategory().getId());
        List<Image> images = imageService.getProjectImages(project.getImageGroup().getId()); // 마크다운 이미지 제외 가져오기
        List<String> imageUrls = imageService.getImageUrls(images);

        UserDetailResponse userDetailResponse = UserMapper.toDetailResponse(project.getSeller());
        List<ProductResponse> productResponses = productService.getAllProducts(project);

        return ProjectMapper.toDetailDto(project, category, imageUrls, userDetailResponse, productResponses);
    }

    // 메인 페이지 조회
    @Transactional(readOnly = true)
    public List<ProjectAllResponse> getProjects() {
        List<Project> projects = projectRepository.findTopNWithImageGroupByOrderByViewsDesc(20);

        AtomicInteger rank = new AtomicInteger(1);
        return projects.stream()
                .map(project -> {
                    String imageUrl = imageService.getImage(project.getImageGroup().getId());
                    int currentRank = rank.getAndIncrement();
                    return ProjectAllResponse.from(project, imageUrl, currentRank);
                })
                .collect(Collectors.toList());
    }

    // ToDo 커서 기반 페이징 조회 메서드 통합 중
    // ToDo 각 case 안에서 requestDto를 다운캐스팅하여 사용하고 있지만, 추후 일반화 할 예정
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public ProjectRecentCursorResponse getProjectsByNew(LocalDateTime lastStartAt, Long lastProjectId, int pageSize) {
        ProjectCursorRequest<LocalDateTime> request = new ProjectCursorRequest<>(lastStartAt, lastProjectId, pageSize);
        return projectQueryRepository.getProjectsByNew(request);
    }

    // 카테고리별 프로젝트 조회 (커서 기반 페이징)
    @Transactional(readOnly = true)
    public ProjectCategoryCursorResponse getProjectsByCategory(ProjectCursorRequest<?> request, Long categoryId,
            SortTypeLatest sortType) {

        return projectFilterQueryRepository.getProjectsByCategory(request, categoryId, sortType);
    }

    // 마감 기한별 프로젝트 조회 (커서 기반 페이징)
    @Transactional(readOnly = true)
    public ProjectDeadlineCursorResponse getProjectsByDeadLine(LocalDateTime lastStartAt,
            SortTypeDeadline sortTypeDeadline, Long lastProjectId, int pageSize) {
        ProjectCursorRequest<LocalDateTime> request = new ProjectCursorRequest<>(lastStartAt, lastProjectId, pageSize);
        return projectFilterQueryRepository.getProjectsByDeadline(request, sortTypeDeadline);
    }

    // 검색 프로젝트 조회 (커서 기반 페이징)
    @Transactional(readOnly = true)
    public ProjectSearchCursorResponse searchProjects(ProjectCursorRequest<?> request, String searchTerms,
            SortTypeLatest sortType) {
        return projectSearchQueryRepository.searchProjects(request, searchTerms, sortType);
    }

    @Transactional(readOnly = true)
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
                                String imageUrl = imageService.getImage(project.getImageGroup().getId());
                                System.out.println(imageUrl);
                                return ProjectMapper.toTopViewResponse(project, imageUrl);
                            })
                            .toList();
                    return new ProjectByPlanGetResponse(planName, items);
                })
                .toList();
    }

    // ToDo 아래 코드에서 TOP 5 뽑는 부분들은 `findTopNWithImageGroupByOrderByViewsDesc` 메서드로
    // 대체할 예정
    @Transactional(readOnly = true)
    public ProjectCategoryTopResponseWrapper getTopView() {
        List<Project> projects = projectRepository.findTopViewedProjectsByCategory();

        // 전체 조회수 기준 Top 5 (카테고리 상관없이)
        List<ProjectTopViewResponse> globalTop5 = projects.stream()
                .sorted(Comparator.comparingLong(Project::getViews).reversed())
                .limit(5)
                .map(project -> {
                    String imageUrl = imageService.getImage(project.getImageGroup().getId());
                    return ProjectMapper.toTopViewResponse(project, imageUrl);
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
                                String imageUrl = imageService.getImage(project.getImageGroup().getId());
                                return ProjectMapper.toTopViewResponse(project, imageUrl);
                            })
                            .toList();
                    return ProjectMapper.toCategoryTopView(category, topViewResponses);
                })
                .toList();

        return new ProjectCategoryTopResponseWrapper(globalTop5, categoryTopViews);
    }

    // 프로젝트 승인 여부로 상태 변경
    @Transactional
    public void updateApprovalStatus(Long projectId, boolean isApproved) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        project.setApprovalStatus(isApproved);
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

    public Long getSellerId(Long projectId) {
        User user = projectRepository.findSellerByProjectId(projectId);
        return user.getId();
    }

}
