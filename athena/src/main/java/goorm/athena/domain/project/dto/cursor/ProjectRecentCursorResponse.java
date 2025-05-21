package goorm.athena.domain.project.dto.cursor;

import goorm.athena.domain.project.dto.res.ProjectRecentResponse;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectRecentCursorResponse(
        List<ProjectRecentResponse> content,
        LocalDateTime nextCursorValue,
        Long nextProjectId,
        Long total
) {
    public static ProjectRecentCursorResponse ofByCreatedAt(List<ProjectRecentResponse> content, Long total) {
        if (content.isEmpty()) {
            return new ProjectRecentCursorResponse(content, null, null, null);
        }

        ProjectRecentResponse last = content.get(content.size() - 1);
        return new ProjectRecentCursorResponse(content, last.createdAt(), last.id(), total);
    }
}
