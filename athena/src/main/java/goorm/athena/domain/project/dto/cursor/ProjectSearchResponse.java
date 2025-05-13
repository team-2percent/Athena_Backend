package goorm.athena.domain.project.dto.cursor;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectSearchResponse<T>(
        List<T> content,
        String searchTerm,
        Long nextProjectId
) {
    public static <T extends ProjectCursorIdentifiable> ProjectSearchResponse<T> ofBySearch(List<T> content, String searchTerm) {
        if (content.isEmpty()) {
            return new ProjectSearchResponse<>(content, searchTerm, null);
        }

        T last = content.get(content.size() - 1);
        return new ProjectSearchResponse<>(content, searchTerm, last.id());
    }
}
