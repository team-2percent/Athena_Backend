package goorm.athena.domain.project.controller;

import goorm.athena.domain.product.dto.res.ProductResponse;
import goorm.athena.domain.project.dto.req.ProjectApprovalRequest;
import goorm.athena.domain.project.dto.req.ProjectCreateRequest;
import goorm.athena.domain.project.dto.res.ProjectIdResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Operation(
            summary = "프로젝트별 상품 목록 조회 API",
            description = """
    특정 프로젝트에 등록된 모든 상품 목록을 조회합니다.<br>
    - `projectId`는 조회 대상 프로젝트의 ID입니다.<br>
    사용 예시: GET /api/project/{projectId}/products
    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "상품 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))
    )
    @GetMapping("/{projectId}/products")
    ResponseEntity<List<ProductResponse>> getProductsByProject(@PathVariable Long projectId);

    @Operation(
            summary = "프로젝트 승인/반려 API",
            description = """
    관리자가 프로젝트를 승인 또는 반려합니다.<br>
    - `approve`: true면 승인, false면 반려로 처리됩니다.<br>
    사용 예시: PATCH /api/project/{projectId}/approval
    """
    )
    @ApiResponse(responseCode = "200", description = "승인/반려 처리 완료")
    @PatchMapping("/{projectId}/approval")
    ResponseEntity<String> updateApprovalStatus(
            @PathVariable Long projectId,
            @RequestBody ProjectApprovalRequest request
    );
}
