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
    private final ProjectMapper projectMapper;

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
        String convertedMarkdown = convertMarkdownIfNeeded(request.contentMarkdown(), markdownFiles, imageGroup);

        validateProject(request);

        Project project = projectMapper.toEntity(request, seller, imageGroup, category, bankAccount, platformPlan,
                convertedMarkdown);
        Project savedProject = projectRepository.save(project);
        createProducts(request.products(), project);

        return projectMapper.toCreateDto(savedProject);
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
    private void validateProject(ProjectCreateRequest request) {
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

        List<String> imagePaths = markdownParser.extractImagePaths(markdown);         // 마크다운 내 url 추출
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

        imageCommandService.deleteImages(project.getImageGroup());  // Image reset
        if (!CollectionUtils.isEmpty(files)) {
            imageCommandService.uploadImages(files, project.getImageGroup());
        } else {
            throw new CustomException(ErrorCode.IMAGE_IS_REQUIRED);
        }
        String convertedMarkdown = convertMarkdownIfNeeded(request.contentMarkdown(), markdownFiles, project.getImageGroup());
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
    }

    // 프로젝트 승인 여부로 상태 변경
    public void updateApprovalStatus(Long projectId, boolean isApproved) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        project.setApprovalStatus(isApproved);
    }

    /**
     * [프로젝트 삭제 Method]
     */
    public void deleteProject(Long projectId) {
        Project project = projectQueryService.getById(projectId);
        ImageGroup imageGroup = project.getImageGroup();

        imageCommandService.deleteImages(imageGroup);           // 이미지 삭제
        productCommandService.deleteAllByProject(project);      // 상품 -> 옵션 삭제
        projectRepository.delete(project);                      // 프로젝트 삭제
        imageGroupCommandService.deleteImageGroup(imageGroup);  // 이미지 그룹 삭제

    }

}
