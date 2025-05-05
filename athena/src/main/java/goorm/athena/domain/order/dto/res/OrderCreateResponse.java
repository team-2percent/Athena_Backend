package goorm.athena.domain.order.dto.res;

import java.time.LocalDateTime;
import java.util.List;

public record OrderCreateResponse(
        Long orderId,
        int totalPrice,
        LocalDateTime orderedAt,
        List<OrderItemCreateResponse> items
) {}