package goorm.athena.domain.category.controller;

import goorm.athena.domain.category.dto.res.CategoryAllGetResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "Category", description = "카테고리 전체 조회 API")
@RequestMapping("/api/category")
public interface CategoryController {

    @Operation(summary = "카테고리 전체 조회 API", description = "전체 카테고리 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "전체 카테고리 목록 조회 완료")
    @GetMapping
    public ResponseEntity<List<CategoryAllGetResponse>> getCategoryAll();
}
