package goorm.athena.domain.project.controller;

import goorm.athena.domain.product.dto.res.ProductResponse;
import goorm.athena.domain.project.dto.cursor.*;
import goorm.athena.domain.project.dto.req.ProjectCreateRequest;
import goorm.athena.domain.project.dto.req.ProjectUpdateRequest;
import goorm.athena.domain.project.dto.res.ProjectIdResponse;
import goorm.athena.domain.project.dto.res.*;
import goorm.athena.domain.project.entity.SortTypeDeadline;
import goorm.athena.domain.project.entity.SortTypeLatest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.format.annotation.DateTimeFormat;

import java.io.IOException;
import java.time.LocalDateTime;

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
    ResponseEntity<ProjectIdResponse> createProject(@RequestBody ProjectCreateRequest request) throws IOException;

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

    @Operation(summary = "프로젝트 수정 API", description = "프로젝트 정보를 수정합니다.<br>" +
            "프로젝트를 수정하기 위해서 필수 항목을 모두 입력해야 합니다.")
    @ApiResponse(responseCode = "200", description = "프로젝트 수정 성공")
    @PutMapping(value = "/{projectId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Void> updateProject(@PathVariable Long projectId,
                                       @RequestPart("projectUpdateRequest") ProjectUpdateRequest projectUpdateRequest);

    @Operation(summary = "프로젝트 삭제 API", description = "프로젝트를 영구적으로 삭제합니다.<br>" +
            "삭제한 프로젝트는 다시 되돌릴 수 없습니다.")
    @ApiResponse(responseCode = "204", description = "프로젝트 삭제 성공")
    @DeleteMapping("/{projectId}")
    ResponseEntity<Void> deleteProject(@PathVariable Long projectId);

    @Operation(summary = "프로젝트 상세 조회", description = "프로젝트 상세 내용을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "프로젝트 상세 조회 성공",
            content = @Content(schema = @Schema(implementation = ProjectDetailResponse.class)))
    @GetMapping("/{projectId}")
    ResponseEntity<ProjectDetailResponse> getProjectDetail(@PathVariable Long projectId);

    @Operation(summary = "프로젝트 전체 조회", description = "프로젝트를 전체로 조회하면서 인기 순으로 정렬합니다. (조회수 순 정렬)<br>" +
            "테스트 시 기본적으로 Pageable은 sort를 가지기 때문에 요청 파라미터에서 sort 키를 없애주시면 됩니다,, !! ")
    @ApiResponse(responseCode = "200", description = "프로젝트 전체 조회 성공",
            content = @Content(schema = @Schema(implementation = ProjectAllResponse.class)))
    @GetMapping("/allList")
    ResponseEntity<List<ProjectAllResponse>> getProjectsAll();

    @Operation(summary = "프로젝트 신규순 조회(무한 페이징)", description = "프로젝트를 신규 순으로 조회합니다. (신규순 정렬)<br>" +
            "페이지는 20개 단위로 구성되며, **맨 처음에는 아무 값도 입력되지 않아도 됩니다.**<br>" +
            "배열 형식으로 20개가 출력되고 맨 마지막에는 'nextCursorValue', 'nextProjectId'가 주어집니다.<br>" +
            "다음 페이지 로딩 시 해당 값을 입력값에 입력 하면 해당 Value의 '다음'값들이 페이지 조회됩니다.<br>")
    @ApiResponse(responseCode = "200", description = "프로젝트 신규순 조회 성공",
            content = @Content(schema = @Schema(implementation = ProjectAllResponse.class)))
    @GetMapping("/recentList")
    public ResponseEntity<ProjectRecentCursorResponse> getProjectsByNew(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorValue,
                                                                                               @RequestParam(required = false) Long lastProjectId, @RequestParam(defaultValue = "20") int pageSize);
    @Operation(summary = "프로젝트 카테고리별 조회", description = "프로젝트를 카테고리별로 조회합니다.<br>" +
            "페이지는 20개 단위로 구성되며, **입력할 카테고리 id를 입력합니다. (입력하지 않으면 모든 카테고리 조회) **<br>" +
            "배열 형식으로 20개가 출력되고 **맨 마지막에는 'nextCursorValue', 'nextProjectId'가 주어집니다.**<br>" +
            "다음 페이지 로딩 시 해당 값을 입력값에 입력 하면 **해당 Value의 '다음'값들이 페이지 조회됩니다.**<br>" +
            "- cursorValue가 “Object”타입으로 여러 값이 들어갈 수 있습니다. (세부 필터)\n" +
            "- 만약, LATEST = 최근일자를 조회하고자 하신다면 “2025-06-07T00:00:00”값이 cursorValue에 들어갑니다.\n" +
            "    \n" +
            "    nextProjectId는 cursorId에 들어갑니다!" +
            "    \n" +
            "    total은 해당 ‘카테고리’의 모든 값을 보여주며 세부 필터로 했을 때의 값들은 해당되지 않습니다." +
            "다음 페이지를 로딩하기 위해 'nextCursorValue', 'nextProjectId' 값을 입력값에 입력 하면 **해당 Value의 '다음'값들이 페이지 조회됩니다.**<br>" +
            """
            <br>⚠️ <b>DEADLINE</b>으로 시작하는 정렬은 사용할 수 없으며, 사용할 시 에러가 리턴됩니다.<br><br>"
                ✅ 사용 가능한 정렬 방식:
                <ul>
                  <li><b style='color:#0074D9;'>최신순 (LATEST)</b></li>
                  <li><b style='color:#0074D9;'>추천순 (RECOMMENDED)</b></li>
                  <li><b style='color:#0074D9;'>인기순 (POPULAR)</b></li>
                  <li><b style='color:#0074D9;'>달성률 순 (SUCCESS_RATE)</b></li>
                </ul>
            """)
    @ApiResponse(responseCode = "200", description = "프로젝트 카테고리별 조회 성공",
            content = @Content(schema = @Schema(implementation = ProjectCategoryResponse.class)))
    @GetMapping("/categoryList")
    public ResponseEntity<ProjectCategoryCursorResponse> getProjectsByCategory(
            @RequestParam(value = "cursorId", required = false) Long cursorId,
            @RequestParam(value = "cursorValue", required = false) Object cursorValue,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam SortTypeLatest sortType);

    @Operation(summary = "프로젝트 마감별 조회", description =
            "프로젝트의 마감일자를 오름차순으로 조회합니다 ( 빨리 끝나는 순)<br>" +
            "페이지는 20개 단위로 구성되며, **맨 처음에는 아무 값도 입력되지 않아도 됩니다.**<br>" +
            "배열 형식으로 20개가 출력되고 **맨 마지막에는 'nextCursorValue', 'nextProjectId'가 주어집니다**.<br>" +
            "다음 페이지 로딩 시 해당 값을 입력값에 입력 하면 **해당 Value의 '다음'값들이 페이지 조회됩니다.**<br>" +
                    "마감일자를 우선으로 정렬됩니다! 1. 마감일자, 2. 세부필터(ex : 성공률, 조회수) 로 정렬됩니다.<br>" +
                    """
        <br>⚠️ <b>DEADLINE</b>으로 시작하지 않는 정렬은 사용할 수 없으며, 그 외에는 에러가 리턴됩니다.<br><br>"
            ✅ 사용 가능한 정렬 방식:
            <ul>
              <li><b style='color:#0074D9;'>마감순 (DEADLINE)</b></li>
              <li><b style='color:#0074D9;'>추천순 (DEADLINE_RECOMMENDED)</b></li>
              <li><b style='color:#0074D9;'>인기순 (DEADLINE_POPULAR)</b></li>
              <li><b style='color:#0074D9;'>달성률 순 (DEADLINE_SUCCESS_RATE)</b></li>
            </ul>
                    """)
    @ApiResponse(responseCode = "200", description = "프로젝트 마감별 조회 성공",
        content = @Content(schema = @Schema(implementation = ProjectDeadlineResponse.class)))
    @GetMapping("/deadlineList")
    ResponseEntity<ProjectDeadlineCursorResponse> getProjectByDeadline(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorValue,
                                                                                               @RequestParam(required = false) Long lastProjectId,
                                                                                               @Parameter(
                                                                                                       description = "마감 정렬 방식",
                                                                                                       example = "DEADLINE",
                                                                                                       schema = @Schema(implementation = SortTypeDeadline.class)
                                                                                               )
                                                                                               @ModelAttribute SortTypeDeadline sortTypeDeadLine,
                                                                                               @RequestParam(defaultValue = "20") int pageSize);


    @Operation(summary = "프로젝트 검색 조회", description = "프로젝트 검색 결과로 제목을 조회합니다.<br>" +
            "페이지는 20개 단위로 구성되며, **기본적으로 검색값 = searchTerm입니다.**<br>" +
            "배열 형식으로 20개가 출력되고 맨 마지막에는 'nextCursorValue', 'nextProjectId'가 주어집니다.<br>" +
            "- cursorValue가 “Object”타입으로 여러 값이 들어갈 수 있습니다. (세부 필터)\n" +
            "- 만약, LATEST = 최근일자를 조회하고자 하신다면 “2025-06-07T00:00:00”값이 cursorValue에 들어갑니다.\n" +
            "    \n" +
            "    nextProjectId는 cursorId에 들어갑니다!" +
            "    \n" +
            "    total은 해당 ‘카테고리’의 모든 값을 보여주며 세부 필터로 했을 때의 값들은 해당되지 않습니다." +
            "다음 페이지를 로딩하기 위해 'nextCursorValue', 'nextProjectId' 값을 입력값에 입력 하면 **해당 Value의 '다음'값들이 페이지 조회됩니다.**<br>" +
            """
            <br>⚠️ <b>DEADLINE</b>으로 시작하는 정렬은 사용할 수 없으며, 사용할 시 에러가 리턴됩니다.<br><br>"
                ✅ 사용 가능한 정렬 방식:
                <ul>
                  <li><b style='color:#0074D9;'>최신순 (LATEST)</b></li>
                  <li><b style='color:#0074D9;'>추천순 (RECOMMENDED)</b></li>
                  <li><b style='color:#0074D9;'>인기순 (POPULAR)</b></li>
                  <li><b style='color:#0074D9;'>달성률 순 (SUCCESS_RATE)</b></li>
                </ul>
            """)
    @ApiResponse(responseCode = "200", description = "프로젝트 검색별 조회 성공",
            content = @Content(schema = @Schema(implementation = ProjectCategoryCursorResponse.class)))
    @GetMapping("/search")
    public ResponseEntity<ProjectSearchCursorResponse> searchProject(@RequestParam String searchTerm,
                                                                                            @RequestParam(required = false) Object cursorValue,
                                                                                            @RequestParam(required = false) Long cursorId,
                                                                                            @RequestParam SortTypeLatest sortType,
                                                                                            @RequestParam(defaultValue = "20") int pageSize);

    @Operation(summary = "프로젝트 메인 카테고리 배너 조회", description = "프로젝트의 메인 배너에서 각 카테고리의 조회수 기준 내림차순 조회합니다.")
    @ApiResponse(responseCode = "200", description = "프로젝트의 카테고리별 조회수 기준 내림차순 조회 성공")
    @GetMapping("/categoryRankingView")
    public ResponseEntity<ProjectCategoryTopResponseWrapper> getProjectByTopView();

    @Operation(summary = "프로젝트 메인 요금별 조회", description = "프로젝트의 메인 페이지에서 요금별로 프로젝트들을 최신순 기준 내림차순 조회합니다.")
    @ApiResponse(responseCode = "200", description = "프로젝트 메인 요금별 조회 최신순 기준 내림차순 조회 성공")
    @GetMapping("/planRankingView")
    public ResponseEntity<List<ProjectByPlanGetResponse>> getProjectByPlan();
}

