package goorm.athena.domain.project.dto.req;

import goorm.athena.domain.project.entity.SortType;

public record ProjectGetDeadLineRequest(
        SortType sortType
) {
}
