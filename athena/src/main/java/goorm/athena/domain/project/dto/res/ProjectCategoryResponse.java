package goorm.athena.domain.project.dto.res;


import goorm.athena.domain.project.entity.Project;

import java.time.LocalDateTime;

public record ProjectCategoryResponse(
        Long id,
        String imageUrl,
        String sellerName,
        String title,
        String description,
        Long achievementRate,
        LocalDateTime createdAt,
        LocalDateTime endAt,
        int daysLeft,
        Long views
){
    public static ProjectCategoryResponse from(Project project, String imageUrl) {
        // 현재 날짜와 종료일 사이의 차이 계산
        long daysLeft = java.time.Duration.between(
                LocalDateTime.now(),
                project.getEndAt()
        ).toDays();

        // 음수 방지 (이미 마감된 경우)
        int safeDaysLeft = (int) Math.max(daysLeft, 0);

        return new ProjectCategoryResponse(
                project.getId(),
                imageUrl,
                project.getSeller().getNickname(),
                project.getTitle(),
                project.getDescription(),
                project.getTotalAmount() == 0 ? 0L : (project.getTotalAmount()) / project.getGoalAmount(),
                project.getCreatedAt(),
                project.getEndAt(),
                safeDaysLeft,
                project.getViews()
        );
    }
}