package goorm.athena.domain.episode.controller;

import goorm.athena.domain.episode.dto.request.EpisodeAddRequest;
import goorm.athena.domain.episode.entity.Episode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Episode", description = "회차 관련 API")
@RequestMapping("/api/episode")
public interface EpisodeController {

    @Operation(summary = "회차 생성 API", description = "현재 웹소설의 새 회차를 생성합니다.")
    @ApiResponse(responseCode = "200", description = "현재 웹소설의 새 회차 생성 성공",
        content = @Content(schema = @Schema(implementation = Episode.class)))
    @PostMapping
    ResponseEntity<Episode> createEpisode(@RequestBody EpisodeAddRequest request);
}
