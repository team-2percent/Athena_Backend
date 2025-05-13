package goorm.athena.domain.project.dto.cursor;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectCursorResponse<T>(
        List<T> content,
        LocalDateTime nextCursorValue,
        Long nextProjectId
) {
    public static <T extends CreatedAtCursorIdentifiable> ProjectCursorResponse<T> ofByCreatedAt(List<T> content) {
        if (content.isEmpty()) {
            return new ProjectCursorResponse<>(content, null, null);
        }

        T last = content.get(content.size() - 1);
        return new ProjectCursorResponse<>(content, last.createdAt(), last.id());
    }

    public static <T extends ProjectCursorIdentifiable> ProjectCursorResponse<T> ofByStartAt(List<T> content) {
        if (content.isEmpty()) {
            return new ProjectCursorResponse<>(content, null, null);
        }

        T last = content.get(content.size() - 1);
        return new ProjectCursorResponse<>(content, last.createdAt(), last.id());
    }

    public static <T extends DeadLineCursorIdentifiable> ProjectCursorResponse<T> ofByEndAt(List<T> content){
        if(content.isEmpty()){
            return new ProjectCursorResponse<>(content, null, null);
        }

        T last = content.get(content.size() - 1);
        return new ProjectCursorResponse<>(content, last.endAt(), last.id());
    }
}
