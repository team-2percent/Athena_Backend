package goorm.athena.domain.project.dto.cursor;

import goorm.athena.domain.project.dto.req.ProjectCursorRequest;

import java.util.List;

public record ProjectCategoryCursorResponse<T>(
        List<ProjectCategoryResponse> content,
        ProjectCursorRequest<T> nextCursor,
        Long totalCount,
        Long categoryId  // 선택적으로 포함 가능
) {
        public static <T> ProjectCategoryCursorResponse<T> of(
                List<ProjectCategoryResponse> content,
                T nextCursorValue,
                Long nextCursorId,
                int size,
                Long totalCount,
                Long categoryId
        ) {
                return new ProjectCategoryCursorResponse<>(
                        content,
                        new ProjectCursorRequest<>(nextCursorValue, nextCursorId, size),
                        totalCount,
                        categoryId
                );
        }
}