package goorm.athena.domain.user.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record MyProjectScrollResponse(
        List<ProjectPreview> content,
        LocalDateTime nextCursorValue,
        Long nextProjectId
) {
    public record ProjectPreview(
            Long projectId,
            String title,
            boolean isCompleted,
            LocalDateTime createdAt,
            LocalDateTime endAt
    ) {}
}