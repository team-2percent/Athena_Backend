package goorm.athena.domain.search.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import goorm.athena.domain.search.dto.Response.SearchResultResponse;

@Tag(name = "Search", description = "검색 관련 API")
@RequestMapping("/api/search")
public interface SearchController {
    @Operation(summary = "검색 목록 조회", description = "검색 목록을 조회합니다.<br>" +
            "검색은 기본적으로 상품명 또는 판매자명으로 검색되며, 필요한 정보는 searchWord 파라미터입니다.<br>" +
            "반환 결과는 상품 목록으로 한 페이지에 20개씩 반환됩니다.")
    @ApiResponse(responseCode = "200", description = "검색 목록 조회 성공")
    @GetMapping
    // ToDo Page<Search> 타입은 추후 Product의 Response Dto 타입으로 변경 필요
    ResponseEntity<SearchResultResponse> searchList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "") @Size(min = 1, message = "검색어는 최소 1글자 이상이어야 합니다.") String searchWord);
}
