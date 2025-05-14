package goorm.athena.domain.project.service;

import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.category.service.CategoryService;
import goorm.athena.domain.image.service.ImageService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.product.dto.req.ProductRequest;
import goorm.athena.domain.product.service.ProductService;
import goorm.athena.domain.project.dto.cursor.*;
import goorm.athena.domain.project.dto.req.ProjectCreateRequest;
import goorm.athena.domain.project.dto.req.ProjectUpdateRequest;
import goorm.athena.domain.project.dto.res.ProjectIdResponse;
import goorm.athena.domain.project.dto.req.ProjectCursorRequest;
import goorm.athena.domain.project.dto.res.*;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.entity.SortType;
import goorm.athena.domain.project.entity.SortTypeLatest;
import goorm.athena.domain.project.mapper.ProjectMapper;
import goorm.athena.domain.project.repository.ProjectQueryRepository;
import goorm.athena.domain.project.repository.ProjectRepository;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
    private final ProjectQueryRepository projectQueryRepository;

    // 프로젝트 생성
    @Transactional
    public ProjectIdResponse createProject(ProjectCreateRequest request) {

        ImageGroup imageGroup = imageGroupService.getById(request.imageGroupId());
        User seller = userService.getUser(request.sellerId());
        Category category = categoryService.getCategoryById(request.categoryId());

        Project project = ProjectMapper.toEntity(request, seller, imageGroup, category);    // 새 프로젝트 생성
        Project savedProject = projectRepository.save(project);         // 프로젝트 저장
        // 프로젝트 생성 시 예외 처리 필요

        // 상품 등록 요청 처리
        List<ProductRequest> productRequests = request.products();
        createProducts(productRequests, project);
        // 상품 생성 예외 처리 추가적으로 있을지 고려

        return ProjectMapper.toCreateDto(savedProject);
    }

    // 프로젝트 수정
    @Transactional
    public void updateProject(Long projectId, ProjectUpdateRequest request, List<MultipartFile> newFiles){
        Project project = getById(projectId);
        Category category = categoryService.getCategoryById(request.categoryId());

        // 마크다운 parshing 로직 추가 해야 함

        project.update(
                category,
                request.title(),
                request.description(),
                request.goalAmount(),
                request.contentMarkdown(),
                request.startAt(),
                request.endAt(),
                request.shippedAt()
        );

        // 상품 업데이트 (삭제 후 다시 등록)
        List<ProductRequest> productUpdateRequests = request.products();
        deleteProducts(project);
        createProducts(productUpdateRequests, project);

        // 이미지 업데이트
        imageService.updateImages(project.getImageGroup(),
                request.existingImageUrls(), newFiles);
    }

    // 프로젝트 삭제
    @Transactional
    public void deleteProject(Long projectId){
        Project project = getById(projectId);

        imageService.deleteImages(project.getImageGroup()); // 이미지 -> 이미지 그룹 삭제
        deleteProducts(project);                            // 상품 -> 옵션 삭제
        projectRepository.delete(project);                  // 프로젝트 삭제
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

    // 상품 리스트 삭제
    private void deleteProducts(Project project) {
        productService.deleteAllByProject(project);
    }

    // Get Project
    public Project getById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
    }

  
    @Transactional(readOnly = true)
    public List<ProjectAllResponse> getProjects() {
        List<Project> projects = projectRepository.findTop20WithImageGroupByOrderByViewsDesc();

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
    public ProjectCursorResponse<ProjectRecentResponse> getProjectsByNew(LocalDateTime lastStartAt, Long lastProjectId, int pageSize) {
        ProjectCursorRequest<LocalDateTime> request = new ProjectCursorRequest<>(lastStartAt, lastProjectId, pageSize);
        return projectQueryRepository.getProjectsByNew(request);
    }

    // 카테고리별 프로젝트 조회 (커서 기반 페이징)
    @Transactional(readOnly = true)
    public ProjectFilterCursorResponse<?> getProjectsByCategory(ProjectCursorRequest<?> request, Long categoryId, SortTypeLatest sortType) {

        return projectQueryRepository.getProjectsByCategory(request, categoryId, sortType);
    }

    // 마감 기한별 프로젝트 조회 (커서 기반 페이징)
    @Transactional(readOnly = true)
    public ProjectCursorResponse<ProjectDeadLineResponse> getProjectsByDeadLine(LocalDateTime lastStartAt, SortType sortType, Long lastProjectId, int pageSize) {
        ProjectCursorRequest<LocalDateTime> request = new ProjectCursorRequest<>(lastStartAt, lastProjectId, pageSize);
        return projectQueryRepository.getProjectsByDeadline(request, sortType);
    }

    // 검색 프로젝트 조회 (커서 기반 페이징)
    @Transactional(readOnly = true)
    public ProjectFilterCursorResponse<ProjectSearchResponse> searchProjects(ProjectCursorRequest<?> request, String searchTerms, int pageSize, SortTypeLatest sortType) {
        return projectQueryRepository.searchProjects(request, searchTerms, sortType);
    }

    public List<ProjectTopViewResponse> getTopView(){
        List<Project> projects = projectRepository.findTopViewedProjectsByCategory();
        return projects.stream()
                .map(project -> {
                    String imageUrl = imageService.getImage(project.getImageGroup().getId());
                    return ProjectMapper.toTopViewResponse(project, imageUrl);
                })
                .toList();
    }
  
    // 후원 기간 종료, 목표금액 달성 , 중복 정산 제외  조건이 충족해야함
    public List<Project> getEligibleProjects(LocalDate baseDate) {
        LocalDateTime endAt = baseDate.plusDays(1).atStartOfDay();
        return projectRepository.findProjectsWithUnsettledOrders(endAt);
    }
}
