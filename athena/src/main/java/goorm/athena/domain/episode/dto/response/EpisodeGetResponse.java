package goorm.athena.domain.episode.dto.response;

public record EpisodeGetResponse(
        Long id,         // 회차 ID
        Long episodeId,  // 회차 Id ( 프론트 )
        String title,    // 회차 제목
        String content   // 회차 내용
) {  }
