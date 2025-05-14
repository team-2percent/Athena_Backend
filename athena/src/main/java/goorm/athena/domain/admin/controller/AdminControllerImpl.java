package goorm.athena.domain.admin.controller;

import goorm.athena.domain.admin.dto.res.ProjectSummaryResponse;
import goorm.athena.domain.admin.service.AdminService;
import goorm.athena.domain.product.dto.res.ProductResponse;
import goorm.athena.domain.product.service.ProductService;
import goorm.athena.domain.project.dto.req.ProjectApprovalRequest;
import goorm.athena.domain.project.service.ProjectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/projects")
public class AdminControllerImpl implements AdminController {

    private final ProjectService projectService;
    private final ProductService productService;
    private final AdminService adminService;

    // 프로젝트 승인/반려
    @PatchMapping("/{projectId}/approval")
    public ResponseEntity<String> updateApprovalStatus(
            @PathVariable Long projectId,
            @RequestBody ProjectApprovalRequest request
    ) {
        projectService.updateApprovalStatus(projectId, request.approve());
        String resultMessage = request.approve() ? "승인되었습니다." : "거절되었습니다.";
        return ResponseEntity.ok(resultMessage);
    }


    @GetMapping
    public ResponseEntity<ProjectSummaryResponse> getProjects(
            @RequestParam(required = false) String keyword,
            @RequestParam(value = "direction", defaultValue = "desc") String direction,
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        ProjectSummaryResponse response = adminService.getProjectList(keyword, direction, page);
        return ResponseEntity.ok(response);
    }
}