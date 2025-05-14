package goorm.athena.domain.project.service;

import goorm.athena.domain.admin.dto.res.ProjectSummaryResponse;
import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.category.service.CategoryService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.product.dto.req.ProductRequest;
import goorm.athena.domain.product.service.ProductService;
import goorm.athena.domain.project.dto.req.ProjectCreateRequest;
import goorm.athena.domain.project.dto.res.ProjectIdResponse;
import goorm.athena.domain.project.entity.ApprovalStatus;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.mapper.ProjectMapper;
import goorm.athena.domain.project.repository.ProjectRepository;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ImageGroupService imageGroupService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final ProductService productService;

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
        }
        else{
            throw new CustomException(ErrorCode.PRODUCT_UPLOAD_FAILED);
        }

        return ProjectMapper.toCreateDto(savedProject);
    }

    public Project getById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
    }

    // 후원 기간 종료, 목표금액 달성 , 중복 정산 제외  조건이 충족해야함
    public List<Project> getEligibleProjects(LocalDate baseDate) {
        LocalDateTime endAt = baseDate.plusDays(1).atStartOfDay();
        return projectRepository.findProjectsWithUnsettledOrders(endAt);
    }

    @Transactional
    public void updateApprovalStatus(Long projectId, boolean isApproved) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        project.setApprovalStatus(isApproved);
    }

}
