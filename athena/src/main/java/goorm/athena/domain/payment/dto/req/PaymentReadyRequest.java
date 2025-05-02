package goorm.athena.domain.payment.dto.req;

import goorm.athena.domain.order.entity.Order;

public record PaymentReadyRequest(
        String itemName,
        Integer quantity,
        Integer totalAmount,
        Long productId,
        String userInfo
) {
    public static PaymentReadyRequest from(Order order) {
        return new PaymentReadyRequest(
                order.getItemName(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getProduct().getId(),
                order.getUser().getNickname()
        );
    }
}