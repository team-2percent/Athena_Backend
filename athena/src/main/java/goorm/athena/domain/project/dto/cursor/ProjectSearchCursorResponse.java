package goorm.athena.domain.project.dto.cursor;

import goorm.athena.domain.project.dto.res.ProjectSearchResponse;
import goorm.athena.domain.project.dto.cursor.ProjectCursorBaseResponse;

import java.util.List;

public record ProjectSearchCursorResponse(
        List<ProjectSearchResponse> content,
        Object nextCursorValue,
        Long nextProjectId,
        Long total
) implements ProjectCursorBaseResponse {
    public static ProjectSearchCursorResponse ofSearch(
            List<ProjectSearchResponse> content,
            Object nextCursorValue,
            Long nextProjectId,
            Long total
    ) {
        return new ProjectSearchCursorResponse(content, nextCursorValue, nextProjectId, total);
    }
}
