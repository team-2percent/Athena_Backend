package goorm.athena.domain.project.dto.cursor;

import java.util.List;

public record ProjectFilterCursorResponse<T>(
        List<T> content,
        Object nextCursorValue,
        Long nextProjectId,
        Long total
) {
    public static ProjectFilterCursorResponse<ProjectCategoryResponse> of(
            List<ProjectCategoryResponse> content,
            Object nextCursorValue,
            Long nextProjectId,
            Long total
    ) {
        return new ProjectFilterCursorResponse<>(content, nextCursorValue, nextProjectId, total);
    }

    public static ProjectFilterCursorResponse<ProjectSearchResponse> ofSearch(
            List<ProjectSearchResponse> content,
            Object nextCursorValue,
            Long nextProjectId,
            Long total
    ) {
        return new ProjectFilterCursorResponse<>(content, nextCursorValue, nextProjectId, total);
    }
}