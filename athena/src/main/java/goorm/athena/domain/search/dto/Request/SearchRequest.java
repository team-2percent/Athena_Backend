package goorm.athena.domain.search.dto.request;

import jakarta.validation.constraints.NotNull;

public record SearchRequest(@NotNull String searchWord) {
}
