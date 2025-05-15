package goorm.athena.domain.project.dto.cursor;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectCursorResponse<T>(
        List<T> content,
        LocalDateTime nextCursorValue,
        Long nextProjectId,
        Long total
) {
    public static ProjectCursorResponse<ProjectRecentResponse> ofByCreatedAt(List<ProjectRecentResponse> content, Long total) {
        if (content.isEmpty()) {
            return new ProjectCursorResponse<>(content, null, null, null);
        }

        ProjectRecentResponse last = content.get(content.size() - 1);
        return new ProjectCursorResponse<>(content, last.createdAt(), last.id(), total);
    }

    public static ProjectCursorResponse<ProjectCategoryResponse> ofByStartAt(List<ProjectCategoryResponse> content, Long total) {
        if (content.isEmpty()) {
            return new ProjectCursorResponse<>(content, null, null, null);
        }

        ProjectCategoryResponse last = content.get(content.size() - 1);
        return new ProjectCursorResponse<>(content, last.createdAt(), last.id(), total);
    }

    public static ProjectCursorResponse<ProjectDeadLineResponse> ofByEndAt(List<ProjectDeadLineResponse> content, Long total){
        if(content.isEmpty()){
            return new ProjectCursorResponse<>(content, null, null, null);
        }

        ProjectDeadLineResponse last = content.get(content.size() - 1);
        return new ProjectCursorResponse<>(content, last.endAt(), last.id(), total);
    }
}
