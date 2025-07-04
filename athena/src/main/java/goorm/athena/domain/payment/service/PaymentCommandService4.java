package goorm.athena.domain.payment.service;

import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.order.service.OrderCommendService;
import goorm.athena.domain.order.service.OrderQueryService;
import goorm.athena.domain.orderitem.entity.OrderItem;
import goorm.athena.domain.orderitem.repository.OrderItemRepository;
import goorm.athena.domain.payment.Infra.V1.KakaoPayImplForMock;
import goorm.athena.domain.payment.dto.req.PaymentReadyRequest;
import goorm.athena.domain.payment.dto.res.KakaoPayReadyResponse;
import goorm.athena.domain.payment.entity.Payment;
import goorm.athena.domain.payment.entity.Status;
import goorm.athena.domain.payment.event.KakaoPayApproveEvent;
import goorm.athena.domain.payment.event.KakaoPayApproveEvent2;
import goorm.athena.domain.payment.repository.PaymentRepository;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.product.service.ProductQueryService;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.service.ProjectQueryService;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCommandService4 {

    private final KakaoPayImplForMock kakaoPayImplForMock;
    private final OrderCommendService orderCommendService;
    private final OrderQueryService orderQueryService;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PlatformTransactionManager transactionManager;
    private final RedissonClient redissonClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisStockService redisStockService;
    private final OrderItemRepository orderItemRepository;
    private final ProjectQueryService projectQueryService;
    private final ProductQueryService productQueryService;

    public KakaoPayReadyResponse readyPayment(Long orderId) {
        Order order = orderQueryService.getById(orderId);
        User user = order.getUser();

        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
            if (payment.getStatus() == Status.APPROVED) {
                throw new CustomException(ErrorCode.ALREADY_PAYMENT_COMPLETED);
            }
            paymentRepository.delete(payment);
            paymentRepository.flush();
        });

        PaymentReadyRequest requestDto = PaymentReadyRequest.from(order);
        KakaoPayReadyResponse response = kakaoPayImplForMock.requestKakaoPayment(requestDto, user, orderId);

        Payment payment = Payment.create(order, user, response.tid(), order.getTotalPrice());
        paymentRepository.save(payment);

        return response;
    }


    // 락 제거 + Lua Script
    public void approvePayment(String pgToken, Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        // 전역락이 아닌 주문 단위로 분산락
        // 동일한 주문에 대해서만 락 적용
        // 서로 다른 주문 병렬 처리
        // 1차: 레디스 중복 요청 차단 (5초 TTL)
        String lockKey = "lock:approvePayment:" + orderId;
        Boolean isLocked = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", 5, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(isLocked)) {
            throw new CustomException(ErrorCode.ALREADY_PAYMENT_COMPLETED); // 빠른 중복 요청 차단
        }
//        // 2차: DB 상태 체크로 최종 확인
//        if (payment.getStatus() == Status.APPROVED) {
//            throw new CustomException(ErrorCode.ALREADY_PAYMENT_COMPLETED); // 로직 상 결제 완료 방지
//        }
        try {

            List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
            if (orderItems.isEmpty()) {
                throw new CustomException(ErrorCode.ORDER_ITEM_NOT_FOUND);
            }

            // 1. Redis에서 차감 (Lua 기반)
            RedisStockDeductionResult result = redisStockService.tryDeductStocks(orderItems);
            if (!result.isSuccess()) {
                throw new CustomException(ErrorCode.INSUFFICIENT_INVENTORY);
            }

//            eventPublisher.publishEvent(new KakaoPayApproveEvent(payment, pgToken));
            eventPublisher.publishEvent(new KakaoPayApproveEvent2(payment, pgToken, orderItems, result));

        } finally {
            redisTemplate.delete(lockKey);
        }
    }
}