package goorm.athena.domain.project.dto.res;

import goorm.athena.domain.project.entity.Project;

import java.time.LocalDateTime;

public record ProjectRecentResponse(
        Long id,
        String imageUrl,
        String sellerName,
        String title,
        String description,
        Integer achievementRate,
        LocalDateTime createdAt,
        LocalDateTime endAt,
        Integer daysLeft
){
    public static ProjectRecentResponse from(Project project, String imageUrl) {
        // 현재 날짜와 종료일 사이의 차이 계산
        long daysLeft = java.time.Duration.between(
                LocalDateTime.now(),
                project.getEndAt()
        ).toDays();

        // 음수 방지 (이미 마감된 경우)
        Integer safeDaysLeft = (int) Math.max(daysLeft, 0);

        return new ProjectRecentResponse(
                project.getId(),
                imageUrl,
                project.getSeller().getNickname(),
                project.getTitle(),
                project.getDescription(),
                project.getTotalAmount() == 0 || project.getGoalAmount() == 0
                            ? 0
                            : (int) (project.getTotalAmount() / project.getGoalAmount()),
                project.getCreatedAt(),
                project.getEndAt(),
                safeDaysLeft
        );
    }
}
