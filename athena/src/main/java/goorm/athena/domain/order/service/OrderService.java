package goorm.athena.domain.order.service;

import goorm.athena.domain.deliveryinfo.repository.DeliveryInfoRepository;
import goorm.athena.domain.order.dto.req.OrderCreateRequest;
import goorm.athena.domain.order.dto.req.OrderItemRequest;
import goorm.athena.domain.order.dto.res.OrderCreateResponse;
import goorm.athena.domain.order.dto.res.OrderItemCreateResponse;
import goorm.athena.domain.order.repository.OrderRepository;
import goorm.athena.domain.orderitem.entity.OrderItem;
import goorm.athena.domain.orderitem.repository.OrderItemRepository;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.product.repository.ProductRepository;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;

import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final DeliveryInfoRepository deliveryInfoRepository;

    @Transactional
    public OrderCreateResponse createOrder(Long userId, OrderCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        DeliveryInfo delivery = deliveryInfoRepository.findById(request.deliveryInfoId())
                .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_NOT_FOUND));

        Order order = Order.create(user, delivery, LocalDateTime.now());
        orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        int totalPrice = 0;

        for (OrderItemRequest item : request.orderItems()) {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

            OrderItem orderItem = OrderItem.of(null, product, item.quantity());
            totalPrice += orderItem.getPrice();
            orderItems.add(orderItem);
        }

        // 주문 상태 및 총 가격 설정
        order.completeOrder(totalPrice);

        // 주문 상세 저장
        orderItemRepository.saveAll(orderItems);

        List<OrderItemCreateResponse> itemResponses = orderItems.stream()
                .map(OrderItemCreateResponse::from)
                .toList();

        return new OrderCreateResponse(order.getId(), totalPrice, order.getOrderedAt(), itemResponses);
    }
}