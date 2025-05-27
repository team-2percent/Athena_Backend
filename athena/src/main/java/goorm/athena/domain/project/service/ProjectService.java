package goorm.athena.domain.project.service;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.bankaccount.service.BankAccountService;
import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.category.service.CategoryService;
import goorm.athena.domain.image.entity.Image;
import goorm.athena.domain.image.service.ImageService;
import goorm.athena.domain.image.service.NasService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.product.dto.req.ProductRequest;
import goorm.athena.domain.product.dto.res.ProductResponse;
import goorm.athena.domain.product.service.ProductService;
import goorm.athena.domain.project.dto.cursor.*;
import goorm.athena.domain.project.dto.req.ProjectCreateRequest;
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
import goorm.athena.domain.user.service.UserService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ImageGroupService imageGroupService;
    private final ImageService imageService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final BankAccountService bankAccountService;
    private final ProjectQueryRepository projectQueryRepository;
    private final ProjectFilterQueryRepository projectFilterQueryRepository;
    private final ProjectSearchQueryRepository projectSearchQueryRepository;
    private final MarkdownParser markdownParser;
    private final PlatformPlanRepository platformPlanRepository;


    /**
     * [프로젝트 등록 Method]
     */
    @Transactional
    public ProjectIdResponse createProject(ProjectCreateRequest request, List<MultipartFile> markdownFiles){
        ImageGroup imageGroup = imageGroupService.getById(request.imageGroupId());
        User seller = userService.getUser(request.sellerId());
        Category category = categoryService.getCategoryById(request.categoryId());
        BankAccount bankAccount = bankAccountService.getAccount(request.sellerId(), request.bankAccountId());
        PlanName planName = PlanName.valueOf(request.platformPlan());
        PlatformPlan platformPlan = platformPlanRepository.findByName(planName);

        validateProduct(request);   // 프로젝트 등록 시 검증

        // 마크다운에 로컬 이미지가 삽입된 경우 이를 이미지 URL로 치환
        String convertedMarkdown = request.contentMarkdown();
        if (!markdownFiles.isEmpty()) {
            try {
                convertedMarkdown = getConvertedMarkdown(markdownFiles, imageGroup, request.contentMarkdown());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        Project project = ProjectMapper.toEntity(request, seller, imageGroup, category, bankAccount, platformPlan, convertedMarkdown);  // 새 프로젝트 생성
        Project savedProject = projectRepository.save(project);                                                                         // 프로젝트 저장

        List<ProductRequest> productRequests = request.products();  // 상품 등록 요청 처리
        createProducts(productRequests, project);
        // 상품 생성 예외 처리 추가적으로 있을지 고려

        return ProjectMapper.toCreateDto(savedProject);
    }

    // 상품 리스트 생성
    private void createProducts(List<ProductRequest> requests, Project project) {
        if (requests != null && !requests.isEmpty()) {
            productService.saveProducts(requests, project);
        }
        else{
            throw new CustomException(ErrorCode.PRODUCT_IS_EMPTY);
        }
    }

    // 프로젝트 등록 검증
    private void validateProduct(ProjectCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();
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

    // 기존 마크다운 -> URL 치환 마크다운
    private String getConvertedMarkdown(List<MultipartFile> markdownFiles, ImageGroup imageGroup, String markdown) throws IOException {
        List<String> imagePaths = markdownParser.extractImagePaths(markdown);                   // 마크다운 내 url 추출
        List<String> realUrls = imageService.uploadMarkdownImages(markdownFiles, imageGroup);   // 이미지 저장 및 이미지 서버 url 반환

        return markdownParser.replaceMarkdown(markdown, imagePaths, realUrls);
    }

    /**
     * [프로젝트 수정 Method]
     */
    @Transactional
    public void updateProject(Long projectId, ProjectUpdateRequest request, List<MultipartFile> files, List<MultipartFile> markdownFiles) {
        Project project = getById(projectId);
        Category category = categoryService.getCategoryById(request.categoryId());
        BankAccount bankAccount = bankAccountService.getPrimaryAccount(request.bankAccountId());

        // 마크다운에 로컬 이미지가 삽입된 경우 이를 이미지 URL로 치환
        String convertedMarkdown = request.contentMarkdown();
        if (!markdownFiles.isEmpty()) {
            try {
                convertedMarkdown = getConvertedMarkdown(markdownFiles, project.getImageGroup(), request.contentMarkdown());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        project.update(
                category,
                bankAccount,
                request.title(),
                request.description(),
                request.goalAmount(),
                convertedMarkdown,
                request.startAt(),
                request.endAt(),
                request.shippedAt()
        );

        // 상품 및 이미지 업데이트 (PUT)
        List<ProductRequest> productUpdateRequests = request.products();
        deleteProducts(project);
        createProducts(productUpdateRequests, project);

        imageService.deleteImages(project.getImageGroup());
        imageService.uploadImages(files, project.getImageGroup().getId());
    }


    /**
     * [프로젝트 삭제 Method]
     */
    @Transactional
    public void deleteProject(Long projectId){
        Project project = getById(projectId);
        ImageGroup imageGroup = project.getImageGroup();

        imageService.deleteImages(imageGroup);              // 이미지 삭제
        deleteProducts(project);                            // 상품 -> 옵션 삭제
        projectRepository.delete(project);                  // 프로젝트 삭제
        imageGroupService.deleteImageGroup(imageGroup);     // 이미지 그룹 삭제

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
    public ProjectDetailResponse getProjectDetail(Long projectId){
        Project project = getById(projectId);

        Category category = categoryService.getCategoryById(project.getCategory().getId());
        List<Image> images = imageService.getProjectImages(project.getImageGroup().getId());    // 마크다운 이미지 제외 가져오기
        List<String> imageUrls = imageService.getImageUrls(images);
        UserDetailResponse userDetailResponse = UserMapper.toDetailResponse(project.getSeller());
        List<ProductResponse> productResponses = productService.getAllProducts(project);

        return ProjectMapper.toDetailDto(project, category, imageUrls, userDetailResponse, productResponses);
    }

    // 메인 페이지 조회
    @Transactional(readOnly = true)
    public List<ProjectAllResponse> getProjects() {
        List<Project> projects = projectRepository.findTop5WithImageGroupByOrderByViewsDesc();

        AtomicInteger rank = new AtomicInteger(1);
        return projects.stream()
                .map(project -> {
                    String imageUrl = imageService.getImage(project.getImageGroup().getId());
                    int currentRank = rank.getAndIncrement();
                    return ProjectAllResponse.from(project, imageUrl, currentRank);
                })
                .collect(Collectors.toList());
    }

    // 최신 프로젝트 조회 (커서 기반 페이징)
    @Transactional(readOnly = true)
    public ProjectRecentCursorResponse getProjectsByNew(LocalDateTime lastStartAt, Long lastProjectId, int pageSize) {
        ProjectCursorRequest<LocalDateTime> request = new ProjectCursorRequest<>(lastStartAt, lastProjectId, pageSize);
        return projectQueryRepository.getProjectsByNew(request);
    }

    // 카테고리별 프로젝트 조회 (커서 기반 페이징)
    @Transactional(readOnly = true)
    public ProjectCategoryCursorResponse getProjectsByCategory(ProjectCursorRequest<?> request, Long categoryId, SortTypeLatest sortType) {

        return projectFilterQueryRepository.getProjectsByCategory(request, categoryId, sortType);
    }

    // 마감 기한별 프로젝트 조회 (커서 기반 페이징)
    @Transactional(readOnly = true)
    public ProjectDeadlineCursorResponse getProjectsByDeadLine(LocalDateTime lastStartAt, SortTypeDeadline sortTypeDeadline, Long lastProjectId, int pageSize) {
        ProjectCursorRequest<LocalDateTime> request = new ProjectCursorRequest<>(lastStartAt, lastProjectId, pageSize);
        return projectFilterQueryRepository.getProjectsByDeadline(request, sortTypeDeadline);
    }

    // 검색 프로젝트 조회 (커서 기반 페이징)
    @Transactional(readOnly = true)
    public ProjectSearchCursorResponse searchProjects(ProjectCursorRequest<?> request, String searchTerms, SortTypeLatest sortType) {
        return projectSearchQueryRepository.searchProjects(request, searchTerms, sortType);
    }

    @Transactional(readOnly = true)
    public List<ProjectByPlanGetResponse> getTopViewByPlan(){
        List<Project> projects = projectRepository.findTop5ProjectsGroupedByPlatformPlan();

        // 요금제 이름(planName) 별로 그룹핑
        Map<String, List<Project>> groupedByPlan = projects.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getPlatformPlan().getName().name(), // PlanName -> String
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // 각 그룹을 ProjectByPlanGetResponse 로 매핑
        return groupedByPlan.entrySet().stream()
                .map(entry -> {
                    String planName = entry.getKey();
                    List<ProjectTopViewResponse> items = entry.getValue().stream()
                            .map(project -> {
                                String imageUrl = imageService.getImage(project.getImageGroup().getId());
                                return ProjectMapper.toTopViewResponse(project, imageUrl);
                            })
                            .toList();
                    return new ProjectByPlanGetResponse(planName, items);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectCategoryTopResponseWrapper getTopView(){
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
                .collect(Collectors.groupingBy(Project::getCategory
                        ,LinkedHashMap::new,
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
        return projectRepository.findProjectsWithUnsettledOrders(endAt);
    }

    // Get Project
    public Project getById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
    }

}
