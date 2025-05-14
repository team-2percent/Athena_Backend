package goorm.athena.domain.project.service;

import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.category.service.CategoryService;
import goorm.athena.domain.image.service.ImageService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.product.dto.req.ProductRequest;
import goorm.athena.domain.product.service.ProductService;
import goorm.athena.domain.project.dto.req.ProjectCreateRequest;
import goorm.athena.domain.project.dto.req.ProjectUpdateRequest;
import goorm.athena.domain.project.dto.res.ProjectIdResponse;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.mapper.ProjectMapper;
import goorm.athena.domain.project.repository.ProjectRepository;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ImageGroupService imageGroupService;
    private final ImageService imageService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final ProductService productService;

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
}
