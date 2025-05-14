package goorm.athena.domain.admin.controller;

import goorm.athena.domain.admin.dto.res.ProjectSummaryResponse;
import goorm.athena.domain.product.dto.res.ProductResponse;
import goorm.athena.domain.project.dto.req.ProjectApprovalRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin", description = "관리자용 페이지 API")
@RequestMapping("/api/admin/projects")
public interface AdminController {

    @Operation(
            summary = "프로젝트 승인/거절 API",
            description = "프로젝트를 승인하거나 거절합니다. 승인 시 approve=true, 거절 시 approve=false 를 전달합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "승인 또는 거절 완료"),
                    @ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음")
            }
    )
    @PatchMapping("/{projectId}/approval")
    ResponseEntity<String> updateApprovalStatus(
            @Parameter(description = "승인 또는 거절할 프로젝트의 ID", example = "1")
            @PathVariable Long projectId,

            @RequestBody ProjectApprovalRequest request
    );

    @Operation(
            summary = "프로젝트 목록 조회",
            description = "관리자가 확인할 수 있는 프로젝트 목록을 조회합니다. 승인 상태가 PENDING인 프로젝트가 우선 정렬되어 보여집니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "프로젝트 목록 조회 성공",
                            content = @Content(schema = @Schema(implementation = ProjectSummaryResponse.class)))
            }
    )
    @GetMapping
    ResponseEntity<ProjectSummaryResponse> getProjects(
            @Parameter(description = "프로젝트 제목 검색어 (선택)") @RequestParam(required = false) String keyword,
            @Parameter(description = "정렬 방향(desc 또는 asc)", example = "desc") @RequestParam(value = "direction", defaultValue = "desc") String direction,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(value = "page", defaultValue = "0") int page
    );
}