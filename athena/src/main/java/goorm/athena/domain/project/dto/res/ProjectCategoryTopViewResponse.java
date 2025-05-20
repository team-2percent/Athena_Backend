package goorm.athena.domain.project.dto.res;

import java.util.List;

public record ProjectCategoryTopViewResponse(
        Long categoryId,
        String categoryName,
        List<ProjectTopViewResponse> items
) { }
