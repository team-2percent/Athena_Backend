package goorm.athena.domain.project.dto.req;

import java.time.LocalDateTime;

public record ProjectQueryLatestRequest(
    LocalDateTime lastStartAt,
    Long lastProjectId,
    int pageSize
) implements ProjectQueryBaseRequest {
}
