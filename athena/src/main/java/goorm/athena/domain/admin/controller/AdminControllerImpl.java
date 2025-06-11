package goorm.athena.domain.admin.controller;

import goorm.athena.domain.admin.dto.res.*;
import goorm.athena.domain.admin.service.AdminRoleCheckService;
import goorm.athena.domain.admin.service.AdminQueryService;
import goorm.athena.domain.coupon.dto.res.CouponGetDetailResponse;
import goorm.athena.domain.coupon.dto.res.CouponGetResponse;
import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.coupon.mapper.CouponMapper;
import goorm.athena.domain.coupon.service.CouponQueryService;
import goorm.athena.domain.project.dto.req.ProjectApprovalRequest;
import goorm.athena.domain.project.dto.res.ProjectDetailResponse;
import goorm.athena.domain.project.service.ProjectCommandService;
import goorm.athena.domain.project.service.ProjectQueryService;
import goorm.athena.domain.settlement.entity.Status;
import goorm.athena.domain.settlement.service.SettlementCommandService;
import goorm.athena.domain.settlement.service.SettlementQueryService;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminControllerImpl implements AdminController {

    private final ProjectQueryService projectQueryService;
    private final ProjectCommandService projectCommandService;
    private final AdminService adminService;
    private final AdminRoleCheckService adminRoleCheckService;
    private final SettlementCommandService settlementCommandService;
    private final SettlementQueryService settlementQueryService;
    private final CouponQueryService couponQueryService;


    // 프로젝트 승인/반려
    @PatchMapping("/project/{projectId}/approval")
    public ResponseEntity<String> updateApprovalStatus(
            @CheckLogin LoginUserRequest loginUserRequest,
            @PathVariable Long projectId,
            @RequestBody ProjectApprovalRequest request
    ) {
        adminRoleCheckService.checkAdmin(loginUserRequest);
        projectCommandService.updateApprovalStatus(projectId, request.approve());
        String resultMessage = request.approve() ? "승인되었습니다." : "거절되었습니다.";
        return ResponseEntity.ok(resultMessage);
    }


    @GetMapping("/project")
    public ResponseEntity<ProjectSummaryResponse> getProjects(
            @CheckLogin LoginUserRequest loginUserRequest,
            @RequestParam(required = false) String keyword,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        adminRoleCheckService.checkAdmin(loginUserRequest);
        ProjectSummaryResponse response = adminQueryService.getProjectList(keyword, sortBy, direction, page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ProjectDetailResponse> getProjectDetail(@PathVariable Long projectId){
        ProjectDetailResponse response = projectQueryService.getProjectDetail(projectId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/settlement")
    public ResponseEntity<SettlementSummaryPageResponse> getSettlements(
            @CheckLogin LoginUserRequest loginUserRequest,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(defaultValue = "0") int page
    ) {
        adminRoleCheckService.checkAdmin(loginUserRequest);
        SettlementSummaryPageResponse response = adminQueryService.getSettlementList(status, year, month, page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/settlement/{settlementId}/info")
    public ResponseEntity<SettlementDetailInfoResponse> getSettlementInfo(
            @PathVariable Long settlementId,
            @CheckLogin LoginUserRequest loginUserRequest
    ) {
        adminRoleCheckService.checkAdmin(loginUserRequest);
        return ResponseEntity.ok(settlementQueryService.getSettlementDetailInfo(settlementId));
    }

    @GetMapping("/settlement/{settlementId}/history")
    public ResponseEntity<SettlementHistoryPageResponse> getSettlementHistories(
            @PathVariable Long settlementId,
            @RequestParam(defaultValue = "0") int page,
            @CheckLogin LoginUserRequest loginUserRequest
    ) {
        adminRoleCheckService.checkAdmin(loginUserRequest);
        return ResponseEntity.ok(settlementQueryService.getSettlementHistories(settlementId, page));
    }

    @GetMapping("/settlement/{settlementId}/product-summary")
    public ResponseEntity<ProductSettlementSummaryResponse> getProductSettlementInfo(
            @PathVariable Long settlementId
    ) {
        ProductSettlementSummaryResponse result = settlementQueryService.getProductSettlementInfo(settlementId);
        return ResponseEntity.ok(result);
    }

    @Override
    @GetMapping("/couponList")
    public ResponseEntity<Page<CouponGetResponse>> getCouponAll(
            @CheckLogin LoginUserRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        Page<Coupon> coupons = couponQueryService.getCoupons(page, size);
        Page<CouponGetResponse> response = coupons.map(CouponMapper::toGetResponse);

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/couponByStatus")
    public ResponseEntity<Page<CouponGetResponse>> getCouponByStatus(
            @CheckLogin LoginUserRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam CouponStatus status){
        Page<Coupon> coupons = couponQueryService.getCouponByStatus(page, size, status);
        Page<CouponGetResponse> response = coupons.map(CouponMapper::toGetResponse);

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{couponId}")
    public ResponseEntity<CouponGetDetailResponse> getCouponDetail(
            @CheckLogin LoginUserRequest request,
            @PathVariable Long couponId) {
        CouponGetDetailResponse response = couponQueryService.getCouponDetail(couponId);
        return ResponseEntity.ok(response);
    }


}