package goorm.athena.domain.episode.controller;

import goorm.athena.domain.episode.dto.request.EpisodeAddRequest;
import goorm.athena.domain.episode.dto.response.EpisodeGetResponse;
import goorm.athena.domain.episode.entity.Episode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Episode", description = "회차 관련 API")
@RequestMapping("/api/episode")
public interface EpisodeController {

    @Operation(summary = "회차 생성 API", description = "현재 웹소설의 새 회차를 생성합니다.<br>" +
            "추가할 작품의 Id와 제목, 내용, 가격이 필요합니다.")
    @ApiResponse(responseCode = "200", description = "현재 웹소설의 새 회차 생성 성공",
        content = @Content(schema = @Schema(implementation = Episode.class)))
    @PostMapping
    ResponseEntity<EpisodeGetResponse> createEpisode(@RequestBody EpisodeAddRequest request);

    @Operation(summary = "회차 조회 API", description = "회차의 ID를 통해 특정 회차를 조회합니다.<br>" +
            "ex) 특정 회차의 내용을 페이지 단위로 조회하기 전, 해당 회차의 제목과 현재 회차 등을 표시하기 위함입니다.")
    @ApiResponse(responseCode = "200", description = "회차 조회 성공",
            content = @Content(schema = @Schema(implementation = EpisodeGetResponse.class)))
    @GetMapping("/{id}")
    ResponseEntity<EpisodeGetResponse> getEpisodeById(@PathVariable Long id);

    @Operation(summary = "회차 내용 페이지 조회 API", description = "특정 회차의 내용을 페이지 단위로 조회합니다.<br>" +
            "회차의 내용들을 '왼쪽', '오른쪽' 페이지 이동 버튼으로 페이지 단위 내용을 조회하기 위함입니다.")
    @ApiResponse(responseCode = "200", description = "회차 내용 페이지 조회 성공",
            content = @Content(schema = @Schema(implementation = String.class)))
    @GetMapping("/{id}/content")
    String getEpisodeContentById(
            @PathVariable Long id,
            @RequestParam int page,
            @RequestParam int pageSize);

    @Operation(summary = "회차 삭제 API", description = "회차의 ID를 통해 특정 회차를 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "회차 삭제 성공")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteEpisode(@PathVariable Long id);

    @Operation(summary = "회차 수정 API", description = "회차의 ID를 통해 특정 회차를 수정합니다.<br>" +
            "기본적으로 회차 생성 API와 유사하며 어느 회차를 수정할 지 PathVariable로 회차 id를 받는 점이 추가되었습니다.<br>" +
            "수정할 작품의 Id와 제목, 내용, 가격이 필요합니다.")
    @ApiResponse(responseCode = "200", description = "회차 수정 성공",
            content = @Content(schema = @Schema(implementation = Episode.class)))
    @PutMapping("/{id}")
    ResponseEntity<EpisodeGetResponse> updateEpisode(@PathVariable Long id, @RequestBody EpisodeAddRequest request);
}
