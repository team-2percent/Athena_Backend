package goorm.athena.domain.project.dto.cursor;

import goorm.athena.domain.project.entity.Project;

import java.time.LocalDateTime;

public record ProjectRecentResponse(
        Long id,
        String title,
        Long views,
        Long achievementRate,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime createdAt,
        String imageUrl
) implements CreatedAtCursorIdentifiable {
    public static ProjectRecentResponse from(Project project, String imageUrl) {
        return new ProjectRecentResponse(
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
