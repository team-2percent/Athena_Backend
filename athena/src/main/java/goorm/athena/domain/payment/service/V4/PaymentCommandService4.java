//package goorm.athena.domain.payment.service.V4;
//
//import goorm.athena.domain.order.entity.Order;
//import goorm.athena.domain.order.service.OrderQueryService;
//import goorm.athena.domain.orderitem.entity.OrderItem;
//import goorm.athena.domain.orderitem.repository.OrderItemRepository;
//import goorm.athena.domain.payment.dto.req.PaymentReadyRequest;
//import goorm.athena.domain.payment.dto.res.KakaoPayReadyResponse;
//import goorm.athena.domain.payment.entity.Payment;
//import goorm.athena.domain.payment.entity.Status;
//import goorm.athena.domain.payment.repository.PaymentRepository;
//import goorm.athena.domain.payment.service.RedisStockService;
//import goorm.athena.domain.payment.service.V1.KakaoPayService2;
//import goorm.athena.domain.user.entity.User;
//import goorm.athena.global.exception.CustomException;
//import goorm.athena.global.exception.ErrorCode;
//
//import org.redisson.api.RLock;
//import org.redisson.api.RedissonClient;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.transaction.PlatformTransactionManager;
//
//import goorm.athena.domain.payment.event.KakaoPayApproveEvent;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.TransactionStatus;
//import org.springframework.transaction.support.DefaultTransactionDefinition;
//
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class PaymentCommandService4 {
//
//    private final KakaoPayService2 kakaoPayService;
//    private final OrderQueryService orderQueryService;
//    private final PaymentRepository paymentRepository;
//    private final ApplicationEventPublisher eventPublisher;
//    private final PlatformTransactionManager transactionManager;
//    private final RedissonClient redissonClient;
//    private final OrderItemRepository orderItemRepository;
//
//    private final RedisStockService redisStockService;
//    private final  RedisTemplate<String, String> redisTemplate;
//
//
//    public KakaoPayReadyResponse readyPayment(Long orderId) {
//        Order order = orderQueryService.getById(orderId);
//        User user = order.getUser();
//
//        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
//            if (payment.getStatus() == Status.APPROVED) {
//                throw new CustomException(ErrorCode.ALREADY_PAYMENT_COMPLETED);
//            }
//            paymentRepository.delete(payment);
//            paymentRepository.flush();
//        });
//
//        PaymentReadyRequest requestDto = PaymentReadyRequest.from(order);
//        KakaoPayReadyResponse response = kakaoPayService.requestKakaoPayment(requestDto, user, orderId);
//
//        Payment payment = Payment.create(order, user, response.tid(), order.getTotalPrice());
//        paymentRepository.save(payment);
//
//        return response;
//    }
//
//    public void approvePayment(String pgToken, Long orderId) {
//
//        Payment payment = paymentRepository.findByOrderId(orderId)
//                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
//
//        Order order = orderQueryService.getById(orderId);
//        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
//
//        if (orderItems == null || orderItems.isEmpty()) {
//            throw new CustomException(ErrorCode.INVALID_ORDER_ORDERITEM);
//        }
//
//        String lockKey = "payment:lock:order:" + orderId;
//        RLock lock = redissonClient.getLock(lockKey);
//
//        try {
//            //(waitTime: 3초, leaseTime: 5초)
//            boolean isLocked = lock.tryLock(3, 5, TimeUnit.SECONDS);
//
//            if (!isLocked) {
//                throw new CustomException(ErrorCode.ALREADY_PAYMENT_COMPLETED);
//            }
//
//            RedisStockDeductionResult result = redisStockService.tryDeductStocks(orderItems);
//            if (!result.isSuccess()) {
//                throw new CustomException(ErrorCode.INSUFFICIENT_INVENTORY);
//            }
//
//            TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
//            try {
//                transactionManager.commit(status);
//            } catch (Exception e) {
//                transactionManager.rollback(status);
//                redisStockService.rollbackStocks(result.getDeductedStocks());
//                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
//            }
//
//            eventPublisher.publishEvent(new KakaoPayApproveEvent(payment, pgToken));
//
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
//        } finally {
//            if (lock.isHeldByCurrentThread()) {
//                lock.unlock();
//            }
//        }
//    }
//}
//
//
