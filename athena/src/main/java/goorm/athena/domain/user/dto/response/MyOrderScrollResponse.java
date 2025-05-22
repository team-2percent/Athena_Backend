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
            Long productId,
            String productTitle,
            String sellerNickname,
            String thumbnailUrl,
            LocalDateTime orderedAt,
            LocalDateTime endAt,
            int achievementRate
    ) {}
}