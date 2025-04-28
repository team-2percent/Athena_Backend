package goorm.athena.domain.novel.controller;

import goorm.athena.domain.novel.dto.req.NovelCreateRequest;
import goorm.athena.domain.novel.dto.res.NovelCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Novel", description = "웹소설 관련 API")
@RequestMapping("/api/novels")
public interface NovelController {

    @Operation(summary = "웹소설 생성 API", description = "새로운 웹소설을 생성합니다.")
    @ApiResponse(responseCode = "200", description = "웹소설 생성 성공", content = @Content(schema = @Schema(implementation = NovelCreateResponse.class)))
    @PostMapping
    ResponseEntity<NovelCreateResponse> createNovel(@RequestBody NovelCreateRequest request);
}