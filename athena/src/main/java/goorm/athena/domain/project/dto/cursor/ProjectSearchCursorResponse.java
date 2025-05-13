package goorm.athena.domain.project.dto.cursor;

import java.util.List;

public record ProjectSearchCursorResponse<T>(
        List<T> content,
        String searchTerm,
        Long nextProjectId
) {
    public static <T extends SearchCursorIdentifiable> ProjectSearchCursorResponse<T> ofBySearch(List<T> content, String searchTerm) {
        if (content.isEmpty()) {
            return new ProjectSearchCursorResponse<>(content, searchTerm, null);
        }

        T last = content.get(content.size() - 1);
        return new ProjectSearchCursorResponse<>(content, searchTerm, last.id());
    }
}
