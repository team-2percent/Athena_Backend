package goorm.athena.domain.project.controller;

import goorm.athena.domain.project.dto.req.ProjectCreateRequest;
import goorm.athena.domain.project.dto.req.ProjectUpdateRequest;
import goorm.athena.domain.project.dto.res.ProjectIdResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Project", description = "Project API")
@RequestMapping("/api/project")
public interface ProjectController {
    @Operation(summary = "프로젝트 초기화 API", description = "프로젝트를 생성하기 위한 정보를 초기화합니다.<br>")
    @ApiResponse(responseCode = "200", description = "프로젝트 초기화 성공")
    @GetMapping
    ResponseEntity<Long> initializeProject();

    @Operation(summary = "프로젝트 생성 API", description = "새로운 프로젝트를 생성합니다.<br>" +
                "프로젝트를 생성하기 위해 관련 정보를 모두 입력해야 합니다.")
    @ApiResponse(responseCode = "200", description = "프로젝트 생성 성공",
        content = @Content(schema = @Schema(implementation = ProjectIdResponse.class)))
    @PostMapping
    ResponseEntity<ProjectIdResponse> createProject(@RequestBody ProjectCreateRequest request);

    @Operation(summary = "프로젝트 수정 API", description = "프로젝트 정보를 수정합니다.<br>" +
            "프로젝트를 수정하기 위해서 필수 항목을 모두 입력해야 합니다.")
    @ApiResponse(responseCode = "200", description = "프로젝트 수정 성공")
    @PutMapping(value = "/{projectId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Void> updateProject(@PathVariable Long projectId,
                                       @RequestParam("projectUpdateRequest") String projectUpdateRequestJson,

                                       @Parameter(description = "새로 업데이트 된 파일들")
                                       @RequestParam("images") List<MultipartFile> newFiles);

    @Operation(summary = "프로젝트 삭제 API", description = "프로젝트를 영구적으로 삭제합니다.<br>" +
            "삭제한 프로젝트는 다시 되돌릴 수 없습니다.")
    @ApiResponse(responseCode = "204", description = "프로젝트 삭제 성공")
    @DeleteMapping("/{projectId}")
    ResponseEntity<Void> deleteProject(@PathVariable Long projectId);
}
