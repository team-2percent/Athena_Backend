package goorm.athena.domain.order.dto.req;

public record OrderItemRequest(
        Long productId,
        int quantity
) {}