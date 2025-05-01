package goorm.athena.domain.search.dto.Reqeust;

import jakarta.validation.constraints.NotNull;

public record SerachRequest(@NotNull String searchWord) {
}
