package goorm.athena.domain.project.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.product.dto.res.ProductResponse;
import goorm.athena.domain.product.service.ProductService;
import goorm.athena.domain.project.dto.req.ProjectApprovalRequest;
import goorm.athena.domain.project.dto.cursor.*;
import goorm.athena.domain.project.dto.req.ProjectCreateRequest;
import goorm.athena.domain.project.dto.req.ProjectUpdateRequest;
import goorm.athena.domain.project.dto.res.ProjectIdResponse;
import goorm.athena.domain.project.dto.res.*;
import goorm.athena.domain.project.entity.SortType;
import goorm.athena.domain.project.service.ProjectService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;


import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/projects")
public class ProjectControllerImpl implements ProjectController {
    private final ProjectService projectService;
    private final ImageGroupService imageGroupService;
    private final ProductService productService;
    private final ObjectMapper objectMapper;

    // 프로젝트 초기 설정 (이미지 그룹 생성)
    @Override
    public ResponseEntity<Long> initializeProject() {
        ImageGroup imageGroup = imageGroupService.createImageGroup(Type.PROJECT);
        return ResponseEntity.ok(imageGroup.getId());
    }

    // 프로젝트 생성
    @Override
    public ResponseEntity<ProjectIdResponse> createProject(@RequestBody ProjectCreateRequest request){
        ProjectIdResponse response = projectService.createProject(request); // 프로젝트 생성 로직
        return ResponseEntity.ok(response);
    }
  
    @GetMapping("/{projectId}/products")
    public ResponseEntity<List<ProductResponse>> getProductsByProject(
            @PathVariable Long projectId
    ) {
        List<ProductResponse> productList = productService.getProductsByProjectId(projectId);
        return ResponseEntity.ok(productList);
    }
    
    // 프로젝트 수정
    @Override
    public ResponseEntity<Void> updateProject(
            @PathVariable Long projectId,
            @RequestParam("projectUpdateRequest") String projectUpdateRequestJson,
            @RequestParam(value = "images", required = false) List<MultipartFile> newFiles){
        ProjectUpdateRequest projectUpdateRequest = convertJsonToDto(projectUpdateRequestJson);
        projectService.updateProject(projectId, projectUpdateRequest, newFiles);
        return ResponseEntity.ok().build();
    }

    // 프로젝트 삭제
    @Override
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId){
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }

    // String -> JSON 처리
    // Swagger test에서만 문제가 있는 부분이라면 추후 삭제 예정
    private ProjectUpdateRequest convertJsonToDto(String json) {
        try {
            return objectMapper.readValue(json, ProjectUpdateRequest.class);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INVALID_JSON_FORMAT);
        }
    }

    @Override
    public ResponseEntity<List<ProjectAllResponse>> getProjectsAll(){
        List<ProjectAllResponse> responses = projectService.getProjects();
        return ResponseEntity.ok(responses);
    }

    @Override
    public ResponseEntity<ProjectCursorResponse<ProjectRecentResponse>> getProjectsByNew(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorValue,
                                                                                         @RequestParam(required = false) Long lastProjectId,
                                                                                         @RequestParam(defaultValue = "20") int pageSize){
        ProjectCursorResponse<ProjectRecentResponse> responses = projectService.getProjectsByNew(cursorValue, lastProjectId, pageSize);
        return ResponseEntity.ok(responses);
    }

    @Override
    public ResponseEntity<ProjectCursorResponse<ProjectCategoryResponse>> getProjectByCategory(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorValue,
                                                                                               @RequestParam(required = false) Long lastProjectId,
                                                                                               @RequestParam Long categoryId,
                                                                                               @ModelAttribute SortType sortType,
                                                                                               @RequestParam(defaultValue = "20") int pageSize){
        ProjectCursorResponse<ProjectCategoryResponse> response = projectService.getProjectsByCategory(cursorValue, categoryId, sortType, lastProjectId, pageSize);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ProjectCursorResponse<ProjectDeadLineResponse>> getProjectByDeadLine(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorValue,
                                                                                               @RequestParam(required = false) Long lastProjectId,
                                                                                               @ModelAttribute SortType sortType,
                                                                                               @RequestParam(defaultValue = "20") int pageSize){
        ProjectCursorResponse<ProjectDeadLineResponse> responses = projectService.getProjectsByDeadLine(cursorValue, sortType, lastProjectId, pageSize);
        return ResponseEntity.ok(responses);

    }

    @Override
    public ResponseEntity<ProjectSearchCursorResponse<ProjectSearchResponse>> searchProject(@RequestParam String searchTerm,
                                                                                            @RequestParam(required = false) Long lastProjectId,
                                                                                            @ModelAttribute SortType sortType,
                                                                                            @RequestParam(defaultValue = "20") int pageSize){
        ProjectSearchCursorResponse<ProjectSearchResponse> response = projectService.searchProjects(searchTerm, sortType, lastProjectId, pageSize);
        return ResponseEntity.ok(response);

    }
}
