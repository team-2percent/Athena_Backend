package goorm.athena.domain.project.dto.cursor;

import goorm.athena.domain.project.entity.Project;

import java.time.LocalDateTime;

public record ProjectSearchResponse(
        Long id,
        String imageUrl,
        String sellerName,
        String title,
        Long achievementRate,
        LocalDateTime createdAt,
        LocalDateTime endAt,
        int daysLeft,
        Long views
){
    public static ProjectSearchResponse from(Project project, String imageUrl) {
        // 현재 날짜와 종료일 사이의 차이 계산
        long daysLeft = java.time.Duration.between(
                LocalDateTime.now(),
                project.getEndAt()
        ).toDays();

        // 음수 방지 (이미 마감된 경우)
        int safeDaysLeft = (int) Math.max(daysLeft, 0);

        return new ProjectSearchResponse(
                project.getId(),
                imageUrl,
                project.getSeller().getNickname(),
                project.getTitle(),
                project.getGoalAmount() / project.getTotalAmount(),
                project.getCreatedAt(),
                project.getEndAt(),
                safeDaysLeft,
                project.getViews()
        );
    }
}
