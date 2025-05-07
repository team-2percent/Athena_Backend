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
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.service.ProjectService;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;


import goorm.athena.domain.user.service.UserService;
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
    private final UserService userService;
    private final DeliveryInfoService deliveryInfoService;
    private final ProductService productService;
    private final ProjectService projectService;

    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
    }

    @Transactional
    public OrderCreateResponse createOrder(Long userId, OrderCreateRequest request) {

        User user = userService.getUserById2(userId);
        DeliveryInfo delivery = deliveryInfoService.getById(request.deliveryInfoId());
        Project project = projectService.getById(request.projectId());

        Order order = Order.create(user, delivery, project, LocalDateTime.now());
        orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();

        int totalQuantity = 0;
        Long totalPrice = 0L;

        for (OrderItemRequest item : request.orderItems()) {
            Product product = productService.getById(item.productId());
            OrderItem orderItem = OrderItem.of(order, product, item.quantity());
            totalPrice += orderItem.getPrice();
            totalQuantity += item.quantity();
            orderItems.add(orderItem);
        }

        order.completeOrder(totalPrice, totalQuantity);
        orderItemRepository.saveAll(orderItems);

        return OrderCreateResponse.from(order, orderItems);
    }


}
