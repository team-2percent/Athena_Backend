package goorm.athena.domain.episode.controller;

import goorm.athena.domain.episode.dto.request.EpisodeAddRequest;
import goorm.athena.domain.episode.dto.response.EpisodeGetResponse;
import goorm.athena.domain.episode.entity.Episode;
import goorm.athena.domain.episode.service.EpisodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/episode")
public class EpisodeController {

    private final EpisodeService episodeService;

    // 회차 조회
    @GetMapping("/{id}")
    public ResponseEntity<EpisodeGetResponse> getEpisodeById(@PathVariable Long id) {
        Episode episode = episodeService.getEpisodeById(id);
        EpisodeGetResponse response = new EpisodeGetResponse(
                episode.getId(),
                episode.getEpisodeId(),
                episode.getTitle(),
                episode.getContent()
        );
        return ResponseEntity.ok(response);
    }

    // 회차 내용 페이지 단위로 가져오기
    @GetMapping("/{id}/content")
    public String getEpisodeContentById(
            @PathVariable Long id,
            @RequestParam int page,
            @RequestParam int pageSize) {
        return episodeService.getEpisodeContentById(id, page, pageSize);
    }

    // 회차 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEpisode(@PathVariable Long id) {
        episodeService.deleteEpisode(id);
        return ResponseEntity.noContent().build();
    }

    // 회차 추가
    @PostMapping
    public ResponseEntity<Episode> addEpisode(@RequestBody EpisodeAddRequest request) {
        episodeService.addEpisode(request.novelId(), request);
        // 생성 로직을 추가
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /*
    // 회차 수정 (나중에 구현 예정)
    @PutMapping("/{id}")
    public ResponseEntity<Episode> updateEpisode(@PathVariable Long id, @RequestBody EpisodeAddRequest request) {
        // 수정 로직을 추가
        return ResponseEntity.ok().build();
    }

 */

}
