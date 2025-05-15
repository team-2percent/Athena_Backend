package goorm.athena.domain.project.dto.cursor;

import goorm.athena.domain.project.entity.Project;

import java.time.LocalDateTime;

public record ProjectAllResponse(
        Long id,
        String sellerName,
        String title,
        Long achievementRate,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String imageUrl,
        int rank,
        Long view
){
    public static ProjectAllResponse from(Project project, String imageUrl, int rank) {
        return new ProjectAllResponse(
                project.getId(),
                project.getSeller().getNickname(),
                project.getTitle(),
                project.getTotalAmount() == 0 || project.getGoalAmount() == 0
                        ? 0L
                        : (project.getTotalAmount()) / project.getGoalAmount(),
                project.getStartAt(),
                project.getEndAt(),
                imageUrl,
                rank,
                project.getViews()
        );
    }
}
