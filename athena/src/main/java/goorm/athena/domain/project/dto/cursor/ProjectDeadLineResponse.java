package goorm.athena.domain.project.dto.cursor;

import goorm.athena.domain.project.entity.Project;

import java.time.LocalDateTime;

public record ProjectDeadLineResponse(
        Long id,
        String title,
        Long views,
        Long achievementRate,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String imageUrl
) implements DeadLineCursorIdentifiable {
    public static ProjectDeadLineResponse from(Project project, String imageUrl) {
        return new ProjectDeadLineResponse(
                project.getId(),
                project.getTitle(),
                project.getViews(),
                project.getGoalAmount() / project.getTotalAmount(),
                project.getStartAt(),
                project.getEndAt(),
                imageUrl
        );
    }
}
