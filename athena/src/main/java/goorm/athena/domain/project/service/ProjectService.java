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
import goorm.athena.domain.project.dto.req.ProjectCursorRequest;
import goorm.athena.domain.project.dto.res.*;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.entity.SortType;
import goorm.athena.domain.project.mapper.ProjectMapper;
import goorm.athena.domain.project.repository.ProjectQueryService;
import goorm.athena.domain.project.repository.ProjectRepository;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
    private final ProjectQueryService projectQueryService;

    @Transactional
    // 프로젝트 생성
    public ProjectIdResponse createProject(ProjectCreateRequest request) {

        ImageGroup imageGroup = imageGroupService.getById(request.imageGroupId());
        User seller = userService.getUser(request.sellerId());
        Category category = categoryService.getCategoryById(request.categoryId());

        Project project = ProjectMapper.toEntity(request, seller, imageGroup, category);    // 새 프로젝트 생성
        Project savedProject = projectRepository.save(project);         // 프로젝트 저장
        // 프로젝트 생성 시 예외 처리 필요

        List<ProductRequest> productRequests = request.products();      // 상품 등록 요청 처리
        if (productRequests != null && !productRequests.isEmpty()) {
            productService.createProducts(productRequests, project);
        } else {
            throw new CustomException(ErrorCode.PRODUCT_UPLOAD_FAILED);
        }

        return ProjectMapper.toCreateDto(savedProject);
    }

    public Project getById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
    }
  
    @Transactional(readOnly = true)
    public List<ProjectAllResponse> getProjects() {
        List<Project> projects = projectRepository.findTop20WithImageGroupByOrderByViewsDesc();
        return projects.stream()
                .map(project -> {
                    String imageUrl = imageService.getImage(project.getImageGroup().getId());
                    return ProjectAllResponse.from(project, imageUrl);
                })
                .collect(Collectors.toList());
    }

    // 최신 프로젝트 조회 (커서 기반 페이징)
    @Transactional(readOnly = true)
    public ProjectCursorResponse<ProjectRecentResponse> getProjectsByNew(LocalDateTime lastStartAt, Long lastProjectId, int pageSize) {
        ProjectCursorRequest<LocalDateTime> request = new ProjectCursorRequest<>(lastStartAt, lastProjectId, pageSize);
        return projectQueryService.getProjectsByNew(request);
    }

    // 카테고리별 프로젝트 조회 (커서 기반 페이징)
    @Transactional(readOnly = true)
    public ProjectCursorResponse<ProjectCategoryResponse> getProjectsByCategory(LocalDateTime lastStartAt, Long categoryId, SortType sortType, Long lastProjectId, int pageSize) {
        ProjectCursorRequest<LocalDateTime> request = new ProjectCursorRequest<>(lastStartAt, lastProjectId, pageSize);
        return projectQueryService.getProjectsByCategory(request, categoryId, sortType);
    }

    // 마감 기한별 프로젝트 조회 (커서 기반 페이징)
    @Transactional(readOnly = true)
    public ProjectCursorResponse<ProjectDeadLineResponse> getProjectsByDeadLine(LocalDateTime lastStartAt, SortType sortType, Long lastProjectId, int pageSize) {
        ProjectCursorRequest<LocalDateTime> request = new ProjectCursorRequest<>(lastStartAt, lastProjectId, pageSize);
        return projectQueryService.getProjectsByDeadline(request, sortType);
    }

    // 검색 프로젝트 조회 (커서 기반 페이징)
    @Transactional(readOnly = true)
    public ProjectSearchResponse<ProjectCategoryResponse> searchProjects( String searchTerms, SortType sortType, Long lastProjectId, int pageSize) {
        ProjectCursorRequest<String> request = new ProjectCursorRequest<>(searchTerms, lastProjectId, pageSize);
        return projectQueryService.searchProjects(request, searchTerms, sortType);
    }
  
    // 후원 기간 종료, 목표금액 달성 , 중복 정산 제외  조건이 충족해야함
    public List<Project> getEligibleProjects(LocalDate baseDate) {
        LocalDateTime endAt = baseDate.plusDays(1).atStartOfDay();
        return projectRepository.findProjectsWithUnsettledOrders(endAt);
    }
}
