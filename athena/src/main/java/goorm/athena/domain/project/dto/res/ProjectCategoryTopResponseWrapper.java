package goorm.athena.domain.project.dto.res;

import java.util.List;

public record ProjectCategoryTopResponseWrapper(
        List<ProjectTopViewResponse> allTopView,
        List<ProjectCategoryTopViewResponse> categoryTopView
) {
}
