package goorm.athena.domain.order.dto.res;

import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.orderitem.entity.OrderItem;

import java.time.LocalDateTime;
import java.util.List;

public record OrderCreateResponse(
        Long orderId,
        long totalPrice,
        LocalDateTime orderedAt,
        List<OrderItemCreateResponse> items
) {
    public static OrderCreateResponse from(Order order, List<OrderItem> orderItems) {
        List<OrderItemCreateResponse> itemResponses = orderItems.stream()
                .map(OrderItemCreateResponse::from)
                .toList();

        return new OrderCreateResponse(order.getId(), order.getTotalPrice(), order.getOrderedAt(), itemResponses);
    }
}