package goorm.athena.domain.project.dto.res;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

public record ProjectCursorResponse<T>(
        List<T> content,
        LocalDateTime nextCursorValue,
        Long nextCursor
) {
    public static <T extends ProjectCursorIdentifiable> ProjectCursorResponse<T> of(List<T> content) {
        if (content.isEmpty()) {
            return new ProjectCursorResponse<>(content, null, null);
        }

        T last = content.get(content.size() - 1);
        return new ProjectCursorResponse<>(content, last.startAt(), last.id());
    }

    private static <T> Long extractLastId(List<T> content) {
        if (content.get(content.size() - 1) instanceof ProjectCategoryResponse response) {
            return response.id();
        }
        throw new IllegalArgumentException("지원하지 않는 타입입니다.");
    }
}
