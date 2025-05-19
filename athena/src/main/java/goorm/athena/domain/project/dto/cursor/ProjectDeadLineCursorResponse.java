package goorm.athena.domain.project.dto.cursor;

import goorm.athena.domain.project.dto.res.ProjectDeadLineResponse;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectDeadLineCursorResponse(
    List<ProjectDeadLineResponse> content,
    LocalDateTime nextCursorValue,
    Long nextProjectId,
    Long total
) {
    public static ProjectDeadLineCursorResponse ofByEndAt(List<ProjectDeadLineResponse> content, Long total) {
        if (content.isEmpty()) {
            return new ProjectDeadLineCursorResponse(content, null, null, null);
        }

        ProjectDeadLineResponse last = content.get(content.size() - 1);
        return new ProjectDeadLineCursorResponse(content, last.endAt(), last.id(), total);
    }
}


