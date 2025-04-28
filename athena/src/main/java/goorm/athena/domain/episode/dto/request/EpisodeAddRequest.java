package goorm.athena.domain.episode.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EpisodeAddRequest(
        @NotNull Long novelId,
        @NotBlank String title,
        @NotBlank String content,
        @NotNull int price
) { }
