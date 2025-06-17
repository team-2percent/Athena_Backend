package goorm.athena.domain.order.service;

import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.order.repository.OrderQueryRepository;
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
    private final OrderQueryRepository orderQueryRepository;

    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
    }

    public Long getSeller(Long orderId) {
        User user = orderQueryRepository.findSellerByOrderId(orderId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        return user.getId();
    }

    public Long getBuyer(Long orderId) {
        User user = orderQueryRepository.findBuyerByOrderId(orderId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        return user.getId();
    }
}