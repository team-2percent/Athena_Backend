package goorm.athena.domain.episode.dto.response;

public record EpisodeGetResponse(
        Long id,         // 회차 ID
        String title,    // 회차 제목
        String content   // 회차 내용
) {  }
