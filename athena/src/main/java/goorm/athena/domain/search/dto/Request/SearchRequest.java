package goorm.athena.domain.search.dto.Request;

import jakarta.validation.constraints.NotNull;

public record SearchRequest(@NotNull String searchWord) {
}
