package goorm.athena.domain.project.dto.cursor;


import goorm.athena.domain.project.entity.Project;

import java.time.LocalDateTime;

public record ProjectCategoryResponse(
        Long id,
        String title,
        Long views,
        Long achievementRate,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime createdAt,
        String imageUrl
) implements ProjectCursorIdentifiable {
    public static ProjectCategoryResponse from(Project project, String imageUrl) {
        return new ProjectCategoryResponse(
                project.getId(),
                project.getTitle(),
                project.getViews(),
                project.getGoalAmount() / project.getTotalAmount(),
                project.getStartAt(),
                project.getEndAt(),
                project.getCreatedAt(),
                imageUrl
        );
    }
}