package goorm.athena.domain.project.dto.cursor;

import goorm.athena.domain.project.dto.res.ProjectCategoryResponse;
import goorm.athena.domain.project.dto.cursor.ProjectCursorBaseResponse;

import java.util.List;

public record ProjectCategoryCursorResponse(
        List<ProjectCategoryResponse> content,
        Object nextCursorValue,
        Long nextProjectId,
        Long total) implements ProjectCursorBaseResponse {
    public static ProjectCategoryCursorResponse of(
            List<ProjectCategoryResponse> content,
            Object nextCursorValue,
            Long nextProjectId,
            Long total) {
        return new ProjectCategoryCursorResponse(content, nextCursorValue, nextProjectId, total);
    }
}