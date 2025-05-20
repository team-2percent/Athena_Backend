package goorm.athena.domain.project.dto.cursor;

import goorm.athena.domain.project.dto.res.ProjectDeadlineResponse;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectDeadlineCursorResponse(
    List<ProjectDeadlineResponse> content,
    LocalDateTime nextCursorValue,
    Long nextProjectId,
    Long total
) {
    public static ProjectDeadlineCursorResponse ofByEndAt(List<ProjectDeadlineResponse> content, Long total) {
        if (content.isEmpty()) {
            return new ProjectDeadlineCursorResponse(content, null, null, null);
        }

        ProjectDeadlineResponse last = content.get(content.size() - 1);
        return new ProjectDeadlineCursorResponse(content, last.endAt(), last.id(), total);
    }
}


