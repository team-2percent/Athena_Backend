package goorm.athena.domain.project.controller;

import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.project.dto.req.ProjectCreateRequest;
import goorm.athena.domain.project.dto.req.ProjectGetCategoryRequest;
import goorm.athena.domain.project.dto.req.ProjectGetDeadLineRequest;
import goorm.athena.domain.project.dto.res.*;
import goorm.athena.domain.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
public class ProjectControllerImpl implements ProjectController {
    private final ProjectService projectService;
    private final ImageGroupService imageGroupService;

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

    @Override
    @GetMapping("/all")
    public ResponseEntity<Page<ProjectAllResponse>> getProjectsAll(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable){
        Page<ProjectAllResponse> responses = projectService.getProjects(pageable);
        return ResponseEntity.ok(responses);
    }

    @Override
    @GetMapping("/new")
    public ResponseEntity<ProjectCursorResponse<ProjectAllResponse>> getProjectsByNew(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorValue,
                                                                                      @RequestParam(required = false) Long lastProjectId,
                                                                                      @RequestParam(defaultValue = "10") int pageSize){
        ProjectCursorResponse<ProjectAllResponse> responses = projectService.getProjectsByNew(cursorValue, lastProjectId, pageSize);
        return ResponseEntity.ok(responses);
    }

    @Override
    @GetMapping("/category")
    public ResponseEntity<ProjectCursorResponse<ProjectCategoryResponse>> getProjectByCategory(@RequestParam(required = false) Long lastProjectId,
                                                                                               @ModelAttribute ProjectGetCategoryRequest request,
                                                                                               @RequestParam(defaultValue = "10") int pageSize){
        ProjectCursorResponse<ProjectCategoryResponse> response = projectService.getProjectsByCategory(request.categoryId(), request.sortType(), lastProjectId, pageSize);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/deadLine")
    public ResponseEntity<ProjectCursorResponse<ProjectDeadLineResponse>> getProjectByDeadLine(@RequestParam(required = false) Long lastProjectId,
                                                                                               @ModelAttribute ProjectGetDeadLineRequest request,
                                                                                               @RequestParam(defaultValue = "10") int pageSize){
        ProjectCursorResponse<ProjectDeadLineResponse> responses = projectService.getProjectsByDeadLine(request.sortType(), lastProjectId, pageSize);
        return ResponseEntity.ok(responses);

    }
}
