package goorm.athena.domain.admin.controller;

import goorm.athena.domain.admin.dto.res.*;
import goorm.athena.domain.coupon.dto.res.CouponGetDetailResponse;
import goorm.athena.domain.coupon.dto.res.CouponGetResponse;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.project.dto.req.ProjectApprovalRequest;
import goorm.athena.domain.project.dto.res.ProjectDetailResponse;
import goorm.athena.domain.settlement.entity.Status;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin", description = "관리자용 페이지 API")
@RequestMapping("/api/admin")
public interface AdminController {

    @Operation(
            summary = "프로젝트 승인/거절 API",
            description = "프로젝트를 승인하거나 거절합니다. 승인 시 approve=true, 거절 시 approve=false 를 전달합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "승인 또는 거절 완료"),
                    @ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음")
            }
    )
    @PatchMapping("/project/{projectId}/approval")
    ResponseEntity<String> updateApprovalStatus(
            @Parameter(hidden = true) @CheckLogin LoginUserRequest loginUserRequest,
            @Parameter(description = "승인 또는 거절할 프로젝트의 ID", example = "1") @PathVariable Long projectId,
            @RequestBody ProjectApprovalRequest request
    );

    @Operation(
            summary = "프로젝트 승인 목록 조회",
            description = "관리자가 확인할 수 있는 프로젝트 목록을 조회합니다. 승인 상태가 PENDING인 프로젝트가 우선 정렬되어 보여집니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "프로젝트 목록 조회 성공",
                            content = @Content(schema = @Schema(implementation = ProjectSummaryResponse.class)))
            }
    )
    @GetMapping("/project")
    ResponseEntity<ProjectSummaryResponse> getProjects(
            @Parameter(hidden = true) @CheckLogin LoginUserRequest loginUserRequest,
            @Parameter(description = "프로젝트 제목 검색어 (선택)") @RequestParam(required = false) String keyword,
            @Parameter(description = "정렬 기준 필드 (예: createdAt, title, nickname)", example = "createdAt")
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @Parameter(description = "정렬 방향(desc 또는 asc)", example = "desc") @RequestParam(value = "direction", defaultValue = "desc") String direction,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(value = "page", defaultValue = "0") int page
    );

    @Operation(
            summary = "프로젝트 상세 조회",
            description = "관리자가 확인할 수 있는 프로젝트의 상세 정보를 조회합니다. 관리자는 프로젝트를 승인/거부할 수 있습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "프로젝트 상세 조회 성공",
                            content = @Content(schema = @Schema(implementation = ProjectDetailResponse.class)))
            }
    )
    @GetMapping("/project/{projectId}")
    ResponseEntity<ProjectDetailResponse> getProjectDetail(@PathVariable Long projectId);


    @Operation(
            summary = "정산 내역 목록 조회",
            description = "관리자가 정산 상태, 연도, 월 기준으로 정산 내역을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정산 내역 조회 성공",
                            content = @Content(schema = @Schema(implementation = SettlementSummaryPageResponse.class)))
            }
    )
    @GetMapping("/settlement")
    ResponseEntity<SettlementSummaryPageResponse> getSettlements(
            @Parameter(hidden = true) @CheckLogin LoginUserRequest loginUserRequest,
            @Parameter(description = "정산 상태 (예: PENDING, COMPLETED)", example = "PENDING")
            @RequestParam(required = false) Status status,

            @Parameter(description = "조회할 연도 (예: 2025)", example = "2025")
            @RequestParam(required = false) Integer year,

            @Parameter(description = "조회할 월 (1~12)", example = "5")
            @RequestParam(required = false) Integer month,

            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page
    );

    @Operation(
            summary = "정산 상세 정보 조회",
            description = "특정 정산 ID의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정산 상세 조회 성공",
                            content = @Content(schema = @Schema(implementation = SettlementDetailInfoResponse.class)))
            }
    )
    @GetMapping("/settlement/{settlementId}/info")
    ResponseEntity<SettlementDetailInfoResponse> getSettlementInfo(
            @Parameter(description = "정산 ID", example = "1") @PathVariable Long settlementId,
            @Parameter(hidden = true) @CheckLogin LoginUserRequest loginUserRequest
    );

    @Operation(
            summary = "정산 히스토리 내역 조회",
            description = "특정 정산 ID에 대한 히스토리 내역을 페이지 단위로 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정산 히스토리 조회 성공",
                            content = @Content(schema = @Schema(implementation = SettlementHistoryPageResponse.class)))
            }
    )
    @GetMapping("/settlement/{settlementId}/history")
    ResponseEntity<SettlementHistoryPageResponse> getSettlementHistories(
            @Parameter(description = "정산 ID", example = "1") @PathVariable Long settlementId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(hidden = true) @CheckLogin LoginUserRequest loginUserRequest
    );

    @Operation(
            summary = "상품별 정산 요약 정보 조회",
            description = "특정 정산 ID에 포함된 상품들의 정산 요약 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "상품 정산 요약 조회 성공",
                            content = @Content(schema = @Schema(implementation = ProductSettlementSummaryResponse.class)))
            }
    )
    @GetMapping("/settlement/{settlementId}/product-summary")
    ResponseEntity<ProductSettlementSummaryResponse> getProductSettlementInfo(
            @Parameter(description = "정산 ID", example = "1") @PathVariable Long settlementId
    );

    @Operation(summary = "쿠폰 페이지 조회 API", description = "쿠폰 목록들을 페이지 형식으로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "쿠폰 목록 페이지 조회 완료")
    @GetMapping("/couponList")
    public ResponseEntity<Page<CouponGetResponse>> getCouponAll(
            @Parameter(hidden = true) @CheckLogin LoginUserRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    @Operation(summary = "쿠폰 상태값 조회 API", description = "쿠폰 목록들을 상태값을 기준으로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "쿠폰 상태 기준 목록 조회 완료")
    @GetMapping("/couponByStatus")
    public ResponseEntity<Page<CouponGetResponse>> getCouponByStatus(
            @Parameter(hidden = true) @CheckLogin LoginUserRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam CouponStatus status);


    @Operation(summary = "쿠폰 상세 정보 조회 API", description = "쿠폰의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "쿠폰의 상세 정보를 조회합니다.")
    @GetMapping("/{couponId}")
    public ResponseEntity<CouponGetDetailResponse> getCouponDetail(
            @Parameter(hidden = true) @CheckLogin LoginUserRequest request,
            @PathVariable Long couponId);

}