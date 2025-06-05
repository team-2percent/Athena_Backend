package goorm.athena.domain.project.dto.req;

import goorm.athena.domain.project.entity.SortTypeLatest;

public record ProjectQuerySearchRequest(
    String searchTerms,
    SortTypeLatest sortType
) implements ProjectQueryBaseRequest {
}
