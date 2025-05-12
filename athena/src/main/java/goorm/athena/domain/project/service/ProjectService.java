package goorm.athena.domain.project.service;

import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.category.service.CategoryService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.product.dto.req.ProductRequest;
import goorm.athena.domain.product.service.ProductService;
import goorm.athena.domain.project.dto.req.ProjectCreateRequest;
import goorm.athena.domain.project.dto.res.ProjectAllResponse;
import goorm.athena.domain.project.dto.res.ProjectCategoryResponse;
import goorm.athena.domain.project.dto.res.ProjectDeadLineResponse;
import goorm.athena.domain.project.dto.res.ProjectIdResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ImageGroupService imageGroupService;
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

        List<ProductRequest> productRequests = request.products();      // 상품 등록 요청 처리
        if (productRequests != null && !productRequests.isEmpty()) {
            productService.createProducts(productRequests, project);
        }
        else{
            throw new CustomException(ErrorCode.PRODUCT_UPLOAD_FAILED);
        }

        return ProjectMapper.toCreateDto(projectRepository.save(project));
    }

    // Get Project
    public Project getById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Page<ProjectAllResponse> getProjects(Pageable pageable){
        return projectRepository.findByOrderByViewsDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Page<ProjectAllResponse> getProjectsByNew(Pageable pageable){
        return projectRepository.findByOrderByStartAtDesc(pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<ProjectCategoryResponse> getProjectByCategory(Long categoryId, SortType sortType, Pageable pageable){
        return projectQueryService.getProjectsByCategoryId(categoryId, sortType, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ProjectDeadLineResponse> getProjectsByDeadLine(SortType sortType, Pageable pageable){
        return projectQueryService.getProjectsByDeadline(sortType, pageable);
    }
}
