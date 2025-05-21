package goorm.athena.domain.project.dto.res;

import java.util.List;

public record ProjectByPlanGetResponse(
        String planName,
        List<ProjectTopViewResponse> items
) {
}
