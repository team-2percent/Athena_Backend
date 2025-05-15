package goorm.athena.domain.project.service;

import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.category.service.CategoryService;
import goorm.athena.domain.image.dto.req.ImageCreateRequest;
import goorm.athena.domain.image.dto.req.ImageUpdateRequest;
import goorm.athena.domain.image.entity.Image;
import goorm.athena.domain.image.service.ImageService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.product.dto.req.ProductRequest;
import goorm.athena.domain.product.dto.res.ProductResponse;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.product.service.ProductService;
import goorm.athena.domain.project.dto.cursor.*;
import goorm.athena.domain.project.dto.req.ProjectCreateRequest;
import goorm.athena.domain.project.dto.req.ProjectUpdateRequest;
import goorm.athena.domain.project.dto.res.ProjectIdResponse;
import goorm.athena.domain.project.dto.req.ProjectCursorRequest;
import goorm.athena.domain.project.dto.res.*;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.entity.SortType;
import goorm.athena.domain.project.mapper.ProjectMapper;
import goorm.athena.domain.project.repository.ProjectQueryRepository;
import goorm.athena.domain.project.repository.ProjectRepository;
import goorm.athena.domain.s3.service.S3Service;
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

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final MarkdownParser markdownParser;

    private final S3Service s3Service;

    // 프로젝트 생성
    @Transactional
    public ProjectIdResponse createProject(ProjectCreateRequest request) {
        ImageGroup imageGroup = imageGroupService.getById(request.imageGroupId());
        User seller = userService.getUser(request.sellerId());
        Category category = categoryService.getCategoryById(request.categoryId());

        // String convertedMarkdown = getConvertedMarkdown(request.markdownImages(), request.contentMarkdown());       // 마크다운 변환
        Project project = ProjectMapper.toEntity(request, seller, imageGroup, category);    // 새 프로젝트 생성
        Project savedProject = projectRepository.save(project);                             // 프로젝트 저장
        // 프로젝트 생성 시 예외 처리 필요

        // 상품 등록 요청 처리
        List<ProductRequest> productRequests = request.products();
        createProducts(productRequests, project);
        // 상품 생성 예외 처리 추가적으로 있을지 고려

        return ProjectMapper.toCreateDto(savedProject);
    }

    // 기존에 있던 마크 다운 -> S3 url이 포함된 마크 다운
    private String getConvertedMarkdown(List<MultipartFile> images, String markdown){
        List<String> imagePaths = markdownParser.extractImagePaths(markdown);  // 마크다운 내 url 추출

        Map<String, String> imagePathToS3Url = new HashMap<>();
        for (int i = 0; i < images.size(); i++) {
            MultipartFile markdownImage = images.get(i);
            String imagePath = imagePaths.get(i);

            String s3Url = s3Service.uploadToS3(markdownImage, imagePath);  // S3 파일 저장
            imageService.uploadImage(imagePath, s3Url);                     // URL + 파일 이름만 DB 저장
            imagePathToS3Url.put(imagePath, s3Url);
        }

        return markdownParser.replaceMarkdown(markdown, imagePathToS3Url);
    }

    // 프로젝트 수정
    @Transactional
    public void updateProject(Long projectId, ProjectUpdateRequest request,
                              List<ImageUpdateRequest> imageRequests){
        Project project = getById(projectId);
        Category category = categoryService.getCategoryById(request.categoryId());

        // 추후 마크다운에서 수정된 이미지 추적하는 코드 추가

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
        imageService.updateImages(project.getImageGroup(), imageRequests);
    }

    // 프로젝트 삭제
    @Transactional
    public void deleteProject(Long projectId){
        Project project = getById(projectId);
        ImageGroup imageGroup = project.getImageGroup();

        imageService.deleteImages(imageGroup);              // 이미지 삭제
        deleteProducts(project);                            // 상품 -> 옵션 삭제
        projectRepository.delete(project);                  // 프로젝트 삭제
        imageGroupService.deleteImageGroup(imageGroup);     // 이미지 그룹 삭제

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

    /**
     * [GET API 관련 Method]
     */

    // 상세 페이지 조회
    @Transactional(readOnly = true)
    public ProjectDetailResponse getProjectDetail(Long projectId){
        Project project = getById(projectId);

        List<Image> images = imageService.getImages(project.getImageGroup());
        List<String> imageUrls = imageService.getImageUrls(images);
        UserDetailResponse userDetailResponse = UserMapper.toDetailResponse(project.getSeller());
        List<ProductResponse> productResponses = productService.getAllProducts(project);

        return ProjectMapper.toDetailDto(project, imageUrls, userDetailResponse, productResponses);
    }

    // 메인 페이지 조회
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
        return projectQueryRepository.getProjectsByNew(request);
    }

    // 카테고리별 프로젝트 조회 (커서 기반 페이징)
    @Transactional(readOnly = true)
    public ProjectCursorResponse<ProjectCategoryResponse> getProjectsByCategory(LocalDateTime lastStartAt, Long categoryId, SortType sortType, Long lastProjectId, int pageSize) {
        ProjectCursorRequest<LocalDateTime> request = new ProjectCursorRequest<>(lastStartAt, lastProjectId, pageSize);
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
    public ProjectSearchCursorResponse<ProjectSearchResponse> searchProjects(String searchTerms, SortType sortType, Long lastProjectId, int pageSize) {
        ProjectCursorRequest<String> request = new ProjectCursorRequest<>(searchTerms, lastProjectId, pageSize);
        return projectQueryRepository.searchProjects(request, searchTerms, sortType);
    }

}
