package goorm.athena.domain.user.dto.response;

import java.time.LocalDateTime;

public record MyProjectScrollRequest(
        LocalDateTime nextCursorValue,
        Long nextProjectId,
        int pageSize
) {}