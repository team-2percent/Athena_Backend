package goorm.athena.domain.project.dto.res;

import java.util.List;

public record ProjectTopViewResponseWrapper(
        List<ProjectTopViewResponse> allTopView,
        List<ProjectCategoryTopViewResponse> categoryTopView
) {
}
