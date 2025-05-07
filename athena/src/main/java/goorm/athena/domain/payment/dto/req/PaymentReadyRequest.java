package goorm.athena.domain.payment.dto.req;

import goorm.athena.domain.order.entity.Order;

public record PaymentReadyRequest(
        String projectName,
        Integer quantity,
        Long totalAmount,
        String userInfo
) {
    public static PaymentReadyRequest from(Order order) {
        return new PaymentReadyRequest(
                order.getProject().getTitle(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getUser().getNickname()
        );
    }
}