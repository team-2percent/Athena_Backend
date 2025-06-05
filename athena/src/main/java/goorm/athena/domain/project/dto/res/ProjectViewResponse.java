package goorm.athena.domain.project.dto.res;

public record ProjectViewResponse(
        Long id,
        String sellerName,
        String title,
        String achievementRate,
        int rank
) {
}
