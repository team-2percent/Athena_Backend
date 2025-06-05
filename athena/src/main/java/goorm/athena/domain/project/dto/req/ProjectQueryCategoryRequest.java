package goorm.athena.domain.project.dto.req;

import goorm.athena.domain.project.entity.SortTypeLatest;

public record ProjectQueryCategoryRequest(
    Long categoryId,
    SortTypeLatest sortType
) implements ProjectQueryBaseRequest {
}
