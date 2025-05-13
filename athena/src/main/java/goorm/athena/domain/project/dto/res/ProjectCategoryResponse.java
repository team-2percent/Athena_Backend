package goorm.athena.domain.project.dto.res;


import goorm.athena.domain.project.entity.Project;

import java.time.LocalDateTime;

public record ProjectCategoryResponse(
        Long id,
        String title,
        Long views,
        Long goalAmount,
        Long totalAmount,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String imageUrl
) implements ProjectCursorIdentifiable{
    public static ProjectCategoryResponse from(Project project, String imageUrl) {
        return new ProjectCategoryResponse(
                project.getId(),
                project.getTitle(),
                project.getViews(),
                project.getGoalAmount(),
                project.getTotalAmount(),
                project.getStartAt(),
                project.getEndAt(),
                imageUrl
        );
    }
}