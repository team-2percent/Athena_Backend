package goorm.athena.domain.order.service;

import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.order.repository.OrderRepository;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;

    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
    }

    public Long getSeller(Long orderId) {
        User user = orderRepository.findSellerByOrderId(orderId);
        return user.getId();
    }

    public Long getBuyer(Long orderId) {
        User user = orderRepository.findBuyerByOrderId(orderId);
        return user.getId();
    }
}