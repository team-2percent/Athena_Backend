package goorm.athena.domain.admin.dto.res;
import org.springframework.data.domain.Page;

import java.util.List;
public record ProjectSummaryResponse(
        List<Item> content,
        PageInfo pageInfo
) {
    public static ProjectSummaryResponse of(Page<Item> page) {
        return new ProjectSummaryResponse(
                page.getContent(),
                new PageInfo(page.getNumber(), page.getTotalPages())
        );
    }

    public record Item(
            Long projectId,
            String title,
            String createdAt,
            String sellerName,
            String approvalStatus
    ) {}

    public record PageInfo(
            int currentPage,
            int totalPages
    ) {}
}