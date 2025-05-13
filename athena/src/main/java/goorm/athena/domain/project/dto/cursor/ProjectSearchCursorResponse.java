package goorm.athena.domain.project.dto.cursor;

import java.util.List;

public record ProjectSearchCursorResponse<T>(
        List<ProjectSearchResponse> content,
        String searchTerm,
        Long nextProjectId
) {
    public static ProjectSearchCursorResponse<ProjectSearchResponse> ofBySearch(List<ProjectSearchResponse> content, String searchTerm) {
        if (content.isEmpty()) {
            return new ProjectSearchCursorResponse<>(content, searchTerm, null);
        }

        ProjectSearchResponse last = content.get(content.size() - 1);
        return new ProjectSearchCursorResponse<>(content, searchTerm, last.id());
    }
}
