package goorm.athena.domain.admin.controller;

import goorm.athena.domain.admin.dto.res.*;
import goorm.athena.domain.admin.service.AdminRoleCheckService;
import goorm.athena.domain.admin.service.AdminService;
import goorm.athena.domain.project.dto.req.ProjectApprovalRequest;
import goorm.athena.domain.project.service.ProjectService;
import goorm.athena.domain.settlement.entity.Status;
import goorm.athena.domain.settlement.service.SettlementService;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminControllerImpl implements AdminController {

    private final ProjectService projectService;
    private final AdminService adminService;
    private final AdminRoleCheckService adminRoleCheckService;
    private final SettlementService settlementService;


    // 프로젝트 승인/반려
    @PatchMapping("/projects/{projectId}/approval")
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


    @GetMapping("/projects")
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

    @GetMapping("/settlements")
    public ResponseEntity<SettlementSummaryPageResponse> getSettlements(
            @CheckLogin LoginUserRequest loginUserRequest,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(defaultValue = "0") int page
    ) {
        adminRoleCheckService.checkAdmin(loginUserRequest);
        SettlementSummaryPageResponse response = adminService.getSettlementList(status, year, month, page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/settlements/{settlementId}/info")
    public ResponseEntity<SettlementDetailInfoResponse> getSettlementInfo(
            @PathVariable Long settlementId
//            @CheckLogin LoginUserRequest loginUserRequest
    ) {
//        adminRoleCheckService.checkAdmin(loginUserRequest);
        return ResponseEntity.ok(settlementService.getSettlementDetailInfo(settlementId));
    }

    @GetMapping("/settlements/{settlementId}/histories")
    public ResponseEntity<SettlementHistoryPageResponse> getSettlementHistories(
            @PathVariable Long settlementId,
            @RequestParam(defaultValue = "0") int page
//            @CheckLogin LoginUserRequest loginUserRequest
    ) {
//        adminRoleCheckService.checkAdmin(loginUserRequest);
        return ResponseEntity.ok(settlementService.getSettlementHistories(settlementId, page));
    }

    @GetMapping("/settlements/{settlementId}/product-summary")
    public ResponseEntity<ProductSettlementSummaryResponse> getProductSettlementInfo(
            @PathVariable Long settlementId
    ) {
        ProductSettlementSummaryResponse result = settlementService.getProductSettlementInfo(settlementId);
        return ResponseEntity.ok(result);
    }
}