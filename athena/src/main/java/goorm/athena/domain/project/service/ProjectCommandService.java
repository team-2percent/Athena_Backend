package goorm.athena.domain.project.service;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.bankaccount.service.BankAccountQueryService;
import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.category.service.CategoryService;
import goorm.athena.domain.image.service.ImageCommandService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.service.ImageGroupCommandService;
import goorm.athena.domain.imageGroup.service.ImageGroupQueryService;
import goorm.athena.domain.product.dto.req.ProductRequest;
import goorm.athena.domain.product.service.ProductCommandService;
import goorm.athena.domain.project.dto.req.ProjectCreateRequest;
import goorm.athena.domain.project.dto.req.ProjectUpdateRequest;
import goorm.athena.domain.project.dto.res.ProjectIdResponse;
import goorm.athena.domain.project.entity.PlanName;
import goorm.athena.domain.project.entity.PlatformPlan;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.mapper.ProjectMapper;
import goorm.athena.domain.project.repository.PlatformPlanRepository;
import goorm.athena.domain.project.repository.ProjectRepository;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.service.UserQueryService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class ProjectCommandService {

    private final ProjectRepository projectRepository;
    private final ImageGroupQueryService imageGroupQueryService;
    private final ImageCommandService imageCommandService;
    private final ImageGroupCommandService imageGroupCommandService;
    private final UserQueryService userQueryService;
    private final CategoryService categoryService;
    private final ProductCommandService productCommandService;
    private final BankAccountQueryService bankAccountQueryService;
    private final MarkdownParser markdownParser;
    private final PlatformPlanRepository platformPlanRepository;
    private final ProjectQueryService projectQueryService;

    /**
     * [프로젝트 등록 Method]
     */
    public ProjectIdResponse createProject(ProjectCreateRequest request, List<MultipartFile> markdownFiles) {
        ImageGroup imageGroup = imageGroupQueryService.getById(request.imageGroupId());
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
            productCommandService.saveProducts(requests, project);
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
        List<String> realUrls = imageCommandService.uploadMarkdownImages(markdownFiles, imageGroup); // 이미지 저장 및 이미지 서버 url 반환

        return markdownParser.replaceMarkdown(markdown, imagePaths, realUrls);
    }

    /**
     * [프로젝트 수정 Method]
     */
    public void updateProject(Long projectId, ProjectUpdateRequest request, List<MultipartFile> files,
                              List<MultipartFile> markdownFiles) {
        Project project = projectQueryService.getById(projectId);
        Category category = categoryService.getCategoryById(request.categoryId());
        BankAccount bankAccount = bankAccountQueryService.getPrimaryAccount(request.bankAccountId());

        // 마크다운 이미지, 대표 이미지 PUT 작업을 위해서 이미지 미리 전체 삭제
        imageCommandService.deleteImages(project.getImageGroup());

        // 마크다운에 로컬 이미지가 삽입된 경우 이를 이미지 URL로 치환
        String convertedMarkdown = convertMarkdownIfNeeded(request.contentMarkdown(), markdownFiles,
                project.getImageGroup());

        if (!CollectionUtils.isEmpty(files)) {
            imageCommandService.uploadImages(files, project.getImageGroup());
        } else {
            throw new CustomException(ErrorCode.IMAGE_IS_REQUIRED);
        }

        // 상품 업데이트 (가격만)
        productCommandService.updateProducts(request.products(), project);

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
    public void deleteProject(Long projectId) {
        Project project = projectQueryService.getById(projectId);
        ImageGroup imageGroup = project.getImageGroup();

        imageCommandService.deleteImages(imageGroup); // 이미지 삭제
        deleteProducts(project); // 상품 -> 옵션 삭제
        projectRepository.delete(project); // 프로젝트 삭제
        imageGroupCommandService.deleteImageGroup(imageGroup); // 이미지 그룹 삭제

    }

    // 상품 리스트 삭제
    private void deleteProducts(Project project) {
        productCommandService.deleteAllByProject(project);
    }

    // 프로젝트 승인 여부로 상태 변경
    public void updateApprovalStatus(Long projectId, boolean isApproved) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        project.setApprovalStatus(isApproved);
    }

}
