package goorm.athena.domain.episode.mapper;

import goorm.athena.domain.episode.dto.request.EpisodeAddRequest;
import goorm.athena.domain.episode.dto.response.EpisodeGetResponse;
import goorm.athena.domain.episode.entity.Episode;

public class EpisodeMapper {

    // EpisodeAddRequest -> Episode 엔티티 변환
    public static Episode toEntity(EpisodeAddRequest request) {
        return Episode.builder()
                .title(request.title())
                .content(request.content())
                .price(request.price())
                .build();
    }

    // Episode -> EpisodeGetResponse DTO 변환
    public static EpisodeGetResponse toResponse(Episode episode) {
        return new EpisodeGetResponse(
                episode.getId(),
                episode.getEpisodeId(),
                episode.getTitle(),
                episode.getContent()
        );
    }
}