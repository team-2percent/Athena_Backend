package goorm.athena.domain.project.dto.res;

public record ProjectTopViewResponse(
        Long id,
        String imageUrl,
        String title,
        Long categoryId
) {
}
