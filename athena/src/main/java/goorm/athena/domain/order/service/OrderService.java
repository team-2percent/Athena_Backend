package goorm.athena.domain.order.service;

import goorm.athena.domain.deliveryinfo.service.DeliveryInfoService;
import goorm.athena.domain.order.dto.req.OrderCreateRequest;
import goorm.athena.domain.order.dto.req.OrderItemRequest;
import goorm.athena.domain.order.dto.res.OrderCreateResponse;
import goorm.athena.domain.order.repository.OrderRepository;
import goorm.athena.domain.orderitem.entity.OrderItem;
import goorm.athena.domain.orderitem.repository.OrderItemRepository;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.product.service.ProductService;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;


import goorm.athena.domain.user.service.UserService;
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
    private final UserService userService;
    private final DeliveryInfoService deliveryInfoService;
    private final ProductService productService;

    @Transactional
    public OrderCreateResponse createOrder(Long userId, OrderCreateRequest request) {

        User user = userService.getUserById2(userId);
        DeliveryInfo delivery = deliveryInfoService.getById(request.deliveryInfoId());

        Order order = Order.create(user, delivery, LocalDateTime.now());
        orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        int totalPrice = 0;

        for (OrderItemRequest item : request.orderItems()) {
            Product product = productService.getById(item.productId());
            OrderItem orderItem = OrderItem.of(null, product, item.quantity());
            totalPrice += orderItem.getPrice();
            orderItems.add(orderItem);
        }

        order.completeOrder(totalPrice);
        orderItemRepository.saveAll(orderItems);

        return OrderCreateResponse.from(order, orderItems);
    }
}
