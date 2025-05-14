package goorm.athena.domain.project.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.project.dto.cursor.*;
import goorm.athena.domain.project.dto.req.ProjectCreateRequest;
import goorm.athena.domain.project.dto.req.ProjectCursorRequest;
import goorm.athena.domain.project.dto.req.ProjectUpdateRequest;
import goorm.athena.domain.project.dto.res.ProjectIdResponse;
import goorm.athena.domain.project.dto.res.*;
import goorm.athena.domain.project.entity.SortType;
import goorm.athena.domain.project.entity.SortTypeLatest;
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
public class ProjectControllerImpl implements ProjectController {
    private final ProjectService projectService;
    private final ImageGroupService imageGroupService;
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

    // 카테고리별 프로젝트 조회 (커서 기반 페이징)
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ProjectFilterCursorResponse<?>> getProjectsByCategory(
            @RequestParam(value = "cursorId", required = false) Long cursorId,
            @RequestParam(value = "cursorValue", required = false) Object cursorValue,
            @RequestParam(value = "size", defaultValue = "2") int size,
            @PathVariable Long categoryId,
            @RequestParam SortTypeLatest sortType) {

        // ProjectCursorRequest DTO 구성
        ProjectCursorRequest<Object> request = new ProjectCursorRequest<>(cursorValue, cursorId, size);

        // 서비스 호출
        ProjectFilterCursorResponse<?> response = projectService.getProjectsByCategory(request, categoryId, sortType);

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
    public ResponseEntity<ProjectFilterCursorResponse<ProjectSearchResponse>> searchProject(@RequestParam String searchTerm,
                                                                                            @RequestParam(required = false) Object cursorValue,
                                                                                            @RequestParam(required = false) Long cursorId,
                                                                                            @RequestParam SortTypeLatest sortType,
                                                                                            @RequestParam(defaultValue = "2") int pageSize){
        ProjectCursorRequest<Object> request = new ProjectCursorRequest<>(cursorValue, cursorId, pageSize);

        ProjectFilterCursorResponse<ProjectSearchResponse> response = projectService.searchProjects(request, searchTerm, pageSize, sortType);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<ProjectTopViewResponse>> getProjectByTopView(){
        List<ProjectTopViewResponse> responses = projectService.getTopView();
        return ResponseEntity.ok(responses);
    }
}
