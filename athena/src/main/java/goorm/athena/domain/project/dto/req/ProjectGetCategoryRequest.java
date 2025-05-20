package goorm.athena.domain.project.dto.req;

import goorm.athena.domain.project.entity.SortTypeDeadline;

public record ProjectGetCategoryRequest(
        Long categoryId,
        SortTypeDeadline sortTypeDeadline
) {
}
