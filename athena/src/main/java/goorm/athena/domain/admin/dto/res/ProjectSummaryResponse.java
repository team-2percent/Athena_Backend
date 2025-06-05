package goorm.athena.domain.admin.dto.res;

import goorm.athena.domain.project.entity.ApprovalStatus;
import goorm.athena.domain.project.entity.PlanName;
import org.springframework.data.domain.Page;

import java.util.List;

public record ProjectSummaryResponse(
        List<Item> content,
        PageInfo pageInfo,
        long pendingCount
) {

    public static ProjectSummaryResponse of(Page<Item> page, long pendingCount) {
        return new ProjectSummaryResponse(
                page.getContent(),
                new PageInfo(page.getNumber(), page.getTotalPages()),pendingCount
        );
    }

    public record Item(
            Long projectId,
            String title,
            String createdAt,
            String sellerName,
            ApprovalStatus isApproved,
            PlanName platformPlan
    ) {}

    public record PageInfo(
            int currentPage,
            int totalPages
    ) {}
}