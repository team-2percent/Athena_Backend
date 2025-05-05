package goorm.athena.domain.order.dto.req;

import java.util.List;

public record OrderCreateRequest(
        Long deliveryInfoId,
        List<OrderItemRequest> orderItems
) {}