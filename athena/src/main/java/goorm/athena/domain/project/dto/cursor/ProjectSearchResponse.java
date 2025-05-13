package goorm.athena.domain.project.dto.cursor;

import goorm.athena.domain.project.entity.Project;

import java.time.LocalDateTime;

public record ProjectSearchResponse(
        Long id,
        String title,
        Long views,
        Long achievementRate,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime createdAt,
        String imageUrl
){
    public static ProjectSearchResponse from(Project project, String imageUrl) {
        return new ProjectSearchResponse(
                project.getId(),
                project.getTitle(),
                project.getViews(),
                project.getTotalAmount() > 0 ? (project.getTotalAmount() * 100) / project.getGoalAmount() : 0L,
                project.getStartAt(),
                project.getEndAt(),
                project.getCreatedAt(),
                imageUrl
        );
    }
}
