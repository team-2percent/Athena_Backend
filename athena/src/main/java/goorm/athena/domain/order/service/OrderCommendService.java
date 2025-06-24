package goorm.athena.domain.order.service;

import goorm.athena.domain.deliveryinfo.service.DeliveryInfoQueryService;
import goorm.athena.domain.order.dto.req.OrderCreateRequest;
import goorm.athena.domain.order.dto.req.OrderItemRequest;
import goorm.athena.domain.order.dto.res.OrderCreateResponse;
import goorm.athena.domain.order.repository.OrderRepository;
import goorm.athena.domain.orderitem.entity.OrderItem;
import goorm.athena.domain.orderitem.repository.OrderItemRepository;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.product.service.ProductQueryService;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.service.ProjectQueryService;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;

import goorm.athena.domain.user.service.UserQueryService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderCommendService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductQueryService productQueryService;
    private final ProjectQueryService projectQueryService;
    private final UserQueryService userQueryService;
    private final DeliveryInfoQueryService deliveryInfoQueryService;


    public OrderCreateResponse createOrder(Long userId, OrderCreateRequest request) {

        User user = userQueryService.getUser(userId);
        DeliveryInfo delivery = deliveryInfoQueryService.getById(request.deliveryInfoId());
        Project project = projectQueryService.getById(request.projectId());

        Order order = Order.create(user, delivery, project, LocalDateTime.now());

        List<OrderItem> orderItems = new ArrayList<>();

        int totalQuantity = 0;
        Long totalPrice = 0L;

        for (OrderItemRequest item : request.orderItems()) {
            Product product = productQueryService.getById(item.productId());
            // 재고 여부 검사
            if (product.getStock() < item.quantity()) {
                throw new CustomException(ErrorCode.INSUFFICIENT_INVENTORY);
            }
//            product.decreaseStock(item.quantity());

            OrderItem orderItem = OrderItem.of(order, product, item.quantity());
            totalPrice += orderItem.getPrice();
            totalQuantity += item.quantity();
            orderItems.add(orderItem);
        }

        order.completeOrder(totalPrice, totalQuantity);
        orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);


        return OrderCreateResponse.from(order, orderItems);
    }


    public void postPaymentProcess(Long orderId) {

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        // 1. 정렬 - 락 획득 순서 통일
        List<OrderItem> sortedItems = orderItems.stream()
                .sorted(Comparator
                        .comparing((OrderItem item) -> item.getProduct().getId())
                        .thenComparing(item -> item.getOrder().getProject().getId()))
                .toList();

        for (OrderItem item : sortedItems) {
            Long productId = item.getProduct().getId();
            Long projectId = item.getOrder().getProject().getId();

            // 2. 비관적 락 걸고 가져오기
            Product product = productQueryService.getProductWithLock(productId);
            Project project = projectQueryService.getProjectWithLock(projectId);

            // 3. 재고 차감 / 누적 금액 증가
            product.decreaseStock(item.getQuantity());
            project.increasePrice(item.getPrice());
        }


//        for (OrderItem item : orderItemRepository.findByOrderId(orderId)) {
//            item.getProduct().decreaseStock(item.getQuantity());
//            item.getOrder().getProject().increasePrice(item.getPrice());
//        }
    }


    public void rollbackStock(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        for (OrderItem item : orderItems) {
            item.getProduct().increaseStock(item.getQuantity());
            item.getOrder().getProject().decreasePrice(item.getPrice());
        }

        Order order = orderItems.get(0).getOrder();
        order.cancel();

    }

}
