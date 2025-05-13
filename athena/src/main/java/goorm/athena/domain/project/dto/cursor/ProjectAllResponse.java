package goorm.athena.domain.project.dto.cursor;

import goorm.athena.domain.project.entity.Project;

import java.time.LocalDateTime;

public record ProjectAllResponse(
        Long id,
        String title,
        Long views,
        Long achievementRate,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime createdAt,
        String imageUrl
) implements ProjectCursorIdentifiable {
    public static ProjectAllResponse from(Project project, String imageUrl) {
        return new ProjectAllResponse(
                project.getId(),
                project.getTitle(),
                project.getViews(),
                project.getTotalAmount() == 0 || project.getGoalAmount() == 0
                        ? 0L
                        : (project.getTotalAmount() * 100) / project.getGoalAmount(),
                project.getStartAt(),
                project.getEndAt(),
                project.getCreatedAt(),
                imageUrl
        );
    }
}
