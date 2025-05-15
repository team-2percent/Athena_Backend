package goorm.athena.domain.project.dto.req;

import goorm.athena.domain.project.entity.SortTypeDeadLine;

public record ProjectGetCategoryRequest(
        Long categoryId,
        SortTypeDeadLine sortTypeDeadLine
) {
}
