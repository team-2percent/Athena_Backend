package goorm.athena.domain.project.dto.req;

import java.time.LocalDateTime;

import goorm.athena.domain.project.entity.SortTypeDeadline;

public record ProjectQueryDeadlineRequest(
    LocalDateTime lastStartAt,
    Long lastProjectId,
    int pageSize,
    SortTypeDeadline sortTypeDeadline
) implements ProjectQueryBaseRequest {
}
