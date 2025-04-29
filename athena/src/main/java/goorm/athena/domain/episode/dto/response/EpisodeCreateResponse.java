package goorm.athena.domain.episode.dto.response;

public record EpisodeCreateResponse(
        Long id,
        Long episodeNumber,
        String title,
        String content
) { }
