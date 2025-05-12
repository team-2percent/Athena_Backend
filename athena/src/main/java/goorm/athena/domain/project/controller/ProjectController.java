package goorm.athena.domain.project.controller;

import goorm.athena.domain.project.dto.req.ProjectCreateRequest;
import goorm.athena.domain.project.dto.res.ProjectIdResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

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

}
