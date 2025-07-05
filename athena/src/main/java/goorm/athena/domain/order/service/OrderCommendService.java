package goorm.athena.domain.order.service;

import goorm.athena.domain.deliveryinfo.service.DeliveryInfoQueryService;
import goorm.athena.domain.order.dto.req.OrderCreateRequest;
import goorm.athena.domain.order.dto.req.OrderItemRequest;
import goorm.athena.domain.order.dto.res.OrderCreateResponse;
import goorm.athena.domain.order.repository.OrderRepository;
import goorm.athena.domain.orderitem.entity.OrderItem;
import goorm.athena.domain.orderitem.repository.OrderItemRepository;
import goorm.athena.domain.payment.service.RedisStockDeductionResult;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.product.service.ProductCommandService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderCommendService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductQueryService productQueryService;
    private final ProductCommandService productCommandService;
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


    // -- 레디스 lua 적용 후 -PaymentService4
    // 	@Async는 새로운 쓰레드에서 실행되기 때문에 기존 트랜잭션 컨텍스트가 전달되지 않습니다. 따라서 @Transactional이 붙어 있어도 무시됩니다.

    public void syncRedisToDb(List<OrderItem> orderItems, RedisStockDeductionResult deducted) {

        log.info("orderItems: {}", orderItems);
        // 1. Redis → DB 재고 동기화
        for (Map.Entry<String, Integer> entry : deducted.getDeductedStocks().entrySet()) {
            String key = entry.getKey(); // "product:stock:1"
            String productIdStr = key.replace("product:stock:", ""); // "1"
            Long productId = Long.parseLong(productIdStr);
            Long redisStock = entry.getValue().longValue();

            Product product = productQueryService.getById(productId);
            Long newStock = product.getStock() - redisStock;

            if (newStock < 0) {
                throw new IllegalStateException("DB 재고가 음수가 될 수 없습니다. productId=" + productId);
            }

            productCommandService.updateStock(productId, newStock);
        }

        // 2. 프로젝트 후원 금액 증가
        for (OrderItem item : orderItems) {
            OrderItem managedItem = orderItemRepository.findById(item.getId())
                    .orElseThrow(() -> new RuntimeException("OrderItem not found"));

            Long projectId = managedItem.getOrder().getProject().getId();
            Project project = projectQueryService.getById(projectId);
            project.increasePrice(managedItem.getPrice());
        }
    }


}
