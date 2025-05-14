package goorm.athena.domain.project.dto.cursor;

import java.util.List;

public record ProjectSearchCursorResponse<T>(
        List<ProjectSearchResponse> content,
        String searchTerm,
        Long nextProjectId,
        Long total
) {
    public static ProjectSearchCursorResponse<ProjectSearchResponse> ofBySearch(List<ProjectSearchResponse> content, String searchTerm, Long total) {
        if (content.isEmpty()) {
            return new ProjectSearchCursorResponse<>(content, searchTerm, null, null);
        }

        ProjectSearchResponse last = content.get(content.size() - 1);
        return new ProjectSearchCursorResponse<>(content, searchTerm, last.id(), total);
    }
}
