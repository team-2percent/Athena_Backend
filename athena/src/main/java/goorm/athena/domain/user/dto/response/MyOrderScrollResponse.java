package goorm.athena.domain.user.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record MyOrderScrollResponse(
        List<Item> content,
        LocalDateTime nextCursorValue,
        Long nextOrderId
) {
    public record Item(
            Long orderId,
            Long projectId,
            String projectName,
            String productName,
            String sellerName,
            String thumbnailUrl,
            LocalDateTime orderedAt,
            LocalDateTime endAt,
            Long achievementRate,
            boolean hasCommented
    ) {}
}