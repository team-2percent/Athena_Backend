package goorm.athena.domain.project.dto.res;

public record ProjectViewResponse(
        Long id,
        String sellerName,
        String projectName,
        String achievementRate,
        int rank
) {
}
