package goorm.athena.domain.project.controller;

import goorm.athena.domain.project.dto.cursor.*;
import goorm.athena.domain.project.dto.req.ProjectCreateRequest;
import goorm.athena.domain.project.dto.req.ProjectUpdateRequest;
import goorm.athena.domain.project.dto.res.ProjectIdResponse;
import goorm.athena.domain.project.dto.res.*;
import goorm.athena.domain.project.entity.SortType;
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

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    ResponseEntity<ProjectIdResponse> createProject(@RequestBody ProjectCreateRequest request);

    @Operation(summary = "프로젝트 수정 API", description = "프로젝트 정보를 수정합니다.<br>" +
            "프로젝트를 수정하기 위해서 필수 항목을 모두 입력해야 합니다.")
    @ApiResponse(responseCode = "200", description = "프로젝트 수정 성공")
    @PutMapping(value = "/{projectId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Void> updateProject(@PathVariable Long projectId,
                                       @RequestParam("projectUpdateRequest") String projectUpdateRequestJson,

                                       @Parameter(description = "새로 업데이트 된 파일들")
                                       @RequestParam(value = "images", required = false) List<MultipartFile> newFiles);

    @Operation(summary = "프로젝트 삭제 API", description = "프로젝트를 영구적으로 삭제합니다.<br>" +
            "삭제한 프로젝트는 다시 되돌릴 수 없습니다.")
    @ApiResponse(responseCode = "204", description = "프로젝트 삭제 성공")
    @DeleteMapping("/{projectId}")
    ResponseEntity<Void> deleteProject(@PathVariable Long projectId);

    @Operation(summary = "프로젝트 전체 조회", description = "프로젝트를 전체로 조회하면서 인기 순으로 정렬합니다. (조회수 순 정렬)<br>" +
            "테스트 시 기본적으로 Pageable은 sort를 가지기 때문에 요청 파라미터에서 sort 키를 없애주시면 됩니다,, !! ")
    @ApiResponse(responseCode = "200", description = "프로젝트 전체 조회 성공",
            content = @Content(schema = @Schema(implementation = ProjectAllResponse.class)))
    @GetMapping("/all")
    public ResponseEntity<List<ProjectAllResponse>> getProjectsAll();

    @Operation(summary = "프로젝트 신규순 조회(무한 페이징)", description = "프로젝트를 신규 순으로 조회합니다. (신규순 정렬)<br>" +
            "페이지는 20개 단위로 구성되며, **맨 처음에는 아무 값도 입력되지 않아도 됩니다.**<br>" +
            "배열 형식으로 20개가 출력되고 맨 마지막에는 'nextCursorValue', 'nextProjectId'가 주어집니다.<br>" +
            "다음 페이지 로딩 시 해당 값을 입력값에 입력 하면 해당 Value의 '다음'값들이 페이지 조회됩니다.<br>")
    @ApiResponse(responseCode = "200", description = "프로젝트 신규순 조회 성공",
            content = @Content(schema = @Schema(implementation = ProjectAllResponse.class)))
    @GetMapping("/new")
    public ResponseEntity<ProjectCursorResponse<ProjectRecentResponse>> getProjectsByNew(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorValue,
                                                                                         @RequestParam(required = false) Long lastProjectId, @Parameter(hidden = true) @RequestParam(defaultValue = "20") int pageSize);
    @Operation(summary = "프로젝트 카테고리별 조회", description = "프로젝트를 카테고리별로 조회합니다.<br>" +
            "페이지는 20개 단위로 구성되며, **맨 처음에는 아무 값도 입력되지 않아도 됩니다.**<br>" +
            "배열 형식으로 20개가 출력되고 맨 마지막에는 'nextCursorValue', 'nextProjectId'가 주어집니다.<br>" +
            "다음 페이지 로딩 시 해당 값을 입력값에 입력 하면 **해당 Value의 '다음'값들이 페이지 조회됩니다.**<br>" +
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
    @GetMapping("/category")
    public ResponseEntity<ProjectCursorResponse<ProjectCategoryResponse>> getProjectByCategory(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorValue,
                                                                                               @RequestParam(required = false) Long lastProjectId,
                                                                                               @RequestParam Long categoryId,
                                                                                               @ModelAttribute SortType sortType,
                                                                                               @Parameter(hidden = true) @RequestParam(defaultValue = "20") int pageSize);

    @Operation(summary = "프로젝트 마감별 조회", description =
            "프로젝트의 마감일자를 오름차순으로 조회합니다 ( 빨리 끝나는 순)<br>" +
            "페이지는 20개 단위로 구성되며, **맨 처음에는 아무 값도 입력되지 않아도 됩니다.**<br>" +
            "배열 형식으로 20개가 출력되고 맨 마지막에는 'nextCursorValue', 'nextProjectId'가 주어집니다.<br>" +
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
        content = @Content(schema = @Schema(implementation = ProjectDeadLineResponse.class)))
    @GetMapping("/deadLine")
    public ResponseEntity<ProjectCursorResponse<ProjectDeadLineResponse>> getProjectByDeadLine(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorValue,
                                                                                               @RequestParam(required = false) Long lastProjectId,
                                                                                               @Parameter(
                                                                                                       description = "마감 정렬 방식",
                                                                                                       example = "DEADLINE",
                                                                                                       schema = @Schema(implementation = SortType.class)
                                                                                               )
                                                                                               @ModelAttribute SortType sortType,
                                                                                               @Parameter(hidden = true) @RequestParam(defaultValue = "20") int pageSize);


    @Operation(summary = "프로젝트 검색 조회", description = "프로젝트 검색 결과로 제목을 조회합니다.<br>" +
            "페이지는 20개 단위로 구성되며, **기본적으로 검색값 = searchTerm과 lastProjectId에는 첫 번째 값 '0'을 입력하셔야 합니다.**<br>" +
            "배열 형식으로 20개가 출력되고 맨 마지막에는 'searchTerm', 'nextProjectId'가 주어집니다.<br>" +
            "다음 페이지 로딩 시 해당 값을 입력값에 입력 하면 **해당 Value의 '다음'값들이 페이지 조회됩니다.**<br>" +
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
            content = @Content(schema = @Schema(implementation = ProjectSearchResponse.class)))
    @GetMapping("/search")
    public ResponseEntity<ProjectSearchCursorResponse<ProjectSearchResponse>> searchProject(@RequestParam String searchTerm,
                                                                                            @RequestParam(required = false) Long lastProjectId,
                                                                                            @ModelAttribute SortType sortType,
                                                                                            @Parameter(hidden = true) @RequestParam(defaultValue = "20") int pageSize);
}

