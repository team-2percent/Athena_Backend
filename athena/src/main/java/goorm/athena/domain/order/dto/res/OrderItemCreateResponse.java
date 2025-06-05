package goorm.athena.domain.order.dto.res;

import goorm.athena.domain.orderitem.entity.OrderItem;

public record OrderItemCreateResponse(
        Long productId,
        String productName,
        int quantity,
        long price
)  {
    public static OrderItemCreateResponse from(OrderItem item) {
        return new OrderItemCreateResponse(
                item.getProduct().getId(),
                item.getProduct().getProductName(),
                item.getQuantity(),
                item.getPrice()
        );
    }
}