package goorm.athena.domain.project.dto.cursor;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectCursorResponse<T>(
        List<T> content,
        LocalDateTime nextCursorValue,
        Long nextProjectId
) {
    public static ProjectCursorResponse<ProjectRecentResponse> ofByCreatedAt(List<ProjectRecentResponse> content) {
        if (content.isEmpty()) {
            return new ProjectCursorResponse<>(content, null, null);
        }

        ProjectRecentResponse last = content.get(content.size() - 1);
        return new ProjectCursorResponse<>(content, last.createdAt(), last.id());
    }

    public static ProjectCursorResponse<ProjectCategoryResponse> ofByStartAt(List<ProjectCategoryResponse> content) {
        if (content.isEmpty()) {
            return new ProjectCursorResponse<>(content, null, null);
        }

        ProjectCategoryResponse last = content.get(content.size() - 1);
        return new ProjectCursorResponse<>(content, last.createdAt(), last.id());
    }

    public static ProjectCursorResponse<ProjectDeadLineResponse> ofByEndAt(List<ProjectDeadLineResponse> content){
        if(content.isEmpty()){
            return new ProjectCursorResponse<>(content, null, null);
        }

        ProjectDeadLineResponse last = content.get(content.size() - 1);
        return new ProjectCursorResponse<>(content, last.endAt(), last.id());
    }
}
