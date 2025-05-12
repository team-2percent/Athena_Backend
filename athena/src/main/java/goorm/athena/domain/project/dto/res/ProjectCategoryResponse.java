package goorm.athena.domain.project.dto.res;


import goorm.athena.domain.project.entity.Project;

import java.time.LocalDateTime;

public record ProjectCategoryResponse<T>(
        Long id,
        String title,
        Long views,
        Long goalAmount,
        Long totalAmount,
        LocalDateTime startAt,
        LocalDateTime endAt
) {
    public static <T> ProjectCategoryResponse<T> from(Project project) {
        return new ProjectCategoryResponse<>(
                project.getId(),
                project.getTitle(),
                project.getViews(),
                project.getGoalAmount(),
                project.getTotalAmount(),
                project.getStartAt(),
                project.getEndAt()
        );
    }
}