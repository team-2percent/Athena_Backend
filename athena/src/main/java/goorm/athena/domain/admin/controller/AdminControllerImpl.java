package goorm.athena.domain.admin.controller;

import goorm.athena.domain.admin.dto.res.ProjectSummaryResponse;
import goorm.athena.domain.admin.service.AdminRoleCheckService;
import goorm.athena.domain.admin.service.AdminService;
import goorm.athena.domain.project.dto.req.ProjectApprovalRequest;
import goorm.athena.domain.project.service.ProjectService;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/projects")
public class AdminControllerImpl implements AdminController {

    private final ProjectService projectService;
    private final AdminService adminService;
    private final AdminRoleCheckService adminRoleCheckService;

    // 프로젝트 승인/반려
    @PatchMapping("/{projectId}/approval")
    public ResponseEntity<String> updateApprovalStatus(
            @CheckLogin LoginUserRequest loginUserRequest,
            @PathVariable Long projectId,
            @RequestBody ProjectApprovalRequest request
    ) {
        adminRoleCheckService.checkAdmin(loginUserRequest);
        projectService.updateApprovalStatus(projectId, request.approve());
        String resultMessage = request.approve() ? "승인되었습니다." : "거절되었습니다.";
        return ResponseEntity.ok(resultMessage);
    }


    @GetMapping
    public ResponseEntity<ProjectSummaryResponse> getProjects(
            @CheckLogin LoginUserRequest loginUserRequest,
            @RequestParam(required = false) String keyword,
            @RequestParam(value = "direction", defaultValue = "desc") String direction,
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        adminRoleCheckService.checkAdmin(loginUserRequest);
        ProjectSummaryResponse response = adminService.getProjectList(keyword, direction, page);
        return ResponseEntity.ok(response);
    }
}