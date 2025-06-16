package goorm.athena.domain.project.controller;

import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.service.ImageGroupCommandService;
import goorm.athena.domain.product.dto.res.ProductResponse;
import goorm.athena.domain.product.service.ProductQueryService;
import goorm.athena.domain.project.dto.cursor.*;
import goorm.athena.domain.project.dto.req.ProjectCreateRequest;
import goorm.athena.domain.project.dto.req.ProjectCursorRequest;
import goorm.athena.domain.project.dto.req.ProjectUpdateRequest;
import goorm.athena.domain.project.dto.res.ProjectIdResponse;
import goorm.athena.domain.project.dto.res.*;
import goorm.athena.domain.project.entity.SortTypeDeadline;
import goorm.athena.domain.project.entity.SortTypeLatest;
import goorm.athena.domain.project.service.ProjectCommandService;
import goorm.athena.domain.project.service.ProjectQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/project")
public class ProjectControllerImpl implements ProjectController {
    private final ProjectQueryService projectQueryService;
    private final ImageGroupCommandService imageGroupCommandService;
    private final ProductQueryService productQueryService;
    private final ProjectCommandService projectCommandService;

    // 프로젝트 초기 설정 (이미지 그룹 생성)
    @Override
    public ResponseEntity<Long> initializeProject() {
        ImageGroup imageGroup = imageGroupCommandService.createImageGroup(Type.PROJECT);
        return ResponseEntity.ok(imageGroup.getId());
    }

    // 프로젝트 생성
    @Override
    public ResponseEntity<ProjectIdResponse> createProject(@RequestPart (value = "request") ProjectCreateRequest request,
                                                           @RequestPart (value = "markdownFiles", required = false) List<MultipartFile> markdownFiles) {
        ProjectIdResponse response = projectCommandService.createProject(request, markdownFiles); // 프로젝트 생성 로직
        return ResponseEntity.ok(response);
    }
  
    @GetMapping("/{projectId}/products")
    public ResponseEntity<List<ProductResponse>> getProductsByProject(
            @PathVariable Long projectId
    ) {
        List<ProductResponse> productList = productQueryService.getProductsByProjectId(projectId);
        return ResponseEntity.ok(productList);
    }
    
    // 프로젝트 수정
    @Override
    public ResponseEntity<Void> updateProject(
            @PathVariable Long projectId,
            @RequestPart(value = "request") ProjectUpdateRequest projectUpdateRequest,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart(value = "markdownFiles", required = false) List<MultipartFile> markdownFiles){
        projectCommandService.updateProject(projectId, projectUpdateRequest, files, markdownFiles);
        return ResponseEntity.ok().build();
    }

    // 프로젝트 삭제
    @Override
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId){
        projectCommandService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }

    /**
     * [GET API]
     */

    // 상세 조회
    @Override
    public ResponseEntity<ProjectDetailResponse> getProjectDetail(@PathVariable Long projectId){
        ProjectDetailResponse response = projectQueryService.getProjectDetail(projectId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<ProjectAllResponse>> getProjectsAll(){
        List<ProjectAllResponse> responses = projectQueryService.getProjects();
        return ResponseEntity.ok(responses);
    }

    @Override
    public ResponseEntity<ProjectRecentCursorResponse> getProjectsByNew(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorValue,
                                                                                               @RequestParam(required = false) Long lastProjectId,
                                                                                               @RequestParam(defaultValue = "20") int pageSize){
        ProjectRecentCursorResponse responses = projectQueryService.getProjectsByNew(cursorValue, lastProjectId, pageSize);
        return ResponseEntity.ok(responses);
    }

    // 카테고리별 프로젝트 조회 (커서 기반 페이징)
    @Override
    public ResponseEntity<ProjectCategoryCursorResponse> getProjectsByCategory(
            @RequestParam(value = "cursorId", required = false) Long cursorId,
            @RequestParam(value = "cursorValue", required = false) Object cursorValue,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam SortTypeLatest sortType) {

        // ProjectCursorRequest DTO 구성
        ProjectCursorRequest<Object> request = new ProjectCursorRequest<>(cursorValue, cursorId, size);

        // 서비스 호출
        ProjectCategoryCursorResponse response = projectQueryService.getProjectsByCategory(request, categoryId, sortType);

        return ResponseEntity.ok(response);

    }

    @Override
    public ResponseEntity<ProjectDeadlineCursorResponse> getProjectByDeadline(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorValue,
                                                                                               @RequestParam(required = false) Long lastProjectId,
                                                                                               @ModelAttribute SortTypeDeadline sortTypeDeadline,
                                                                                               @RequestParam(defaultValue = "20") int pageSize){
        ProjectDeadlineCursorResponse responses = projectQueryService.getProjectsByDeadLine(cursorValue, sortTypeDeadline, lastProjectId, pageSize);
        return ResponseEntity.ok(responses);

    }

    @Override
    public ResponseEntity<ProjectSearchCursorResponse> searchProject(@RequestParam String searchTerm,
                                                                                            @RequestParam(required = false) Object cursorValue,
                                                                                            @RequestParam(required = false) Long cursorId,
                                                                                            @RequestParam SortTypeLatest sortType,
                                                                                            @RequestParam(defaultValue = "20") int pageSize){
        ProjectCursorRequest<Object> request = new ProjectCursorRequest<>(cursorValue, cursorId, pageSize);

        ProjectSearchCursorResponse response = projectQueryService.searchProjects(request, searchTerm, sortType);
        return ResponseEntity.ok(response);

    }

    @Override
    public ResponseEntity<ProjectCategoryTopResponseWrapper> getProjectByTopView(){
        ProjectCategoryTopResponseWrapper responses = projectQueryService.getTopView();
        return ResponseEntity.ok(responses);
    }

    @Override
    public ResponseEntity<List<ProjectByPlanGetResponse>> getProjectByPlan(){
        List<ProjectByPlanGetResponse> response = projectQueryService.getTopViewByPlan();
        return ResponseEntity.ok(response);
    }
}
