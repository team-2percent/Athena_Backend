package goorm.athena.domain.project.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.project.dto.req.ProjectCreateRequest;
import goorm.athena.domain.project.dto.req.ProjectUpdateRequest;
import goorm.athena.domain.project.dto.res.ProjectIdResponse;
import goorm.athena.domain.project.service.ProjectService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            @RequestParam("images") List<MultipartFile> newFiles){
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
}
