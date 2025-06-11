package goorm.athena.domain.payment.service;

import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.order.service.OrderCommendService;
import goorm.athena.domain.order.service.OrderQueryService;
import goorm.athena.domain.payment.dto.req.PaymentApproveRequest;
import goorm.athena.domain.payment.dto.req.PaymentReadyRequest;
import goorm.athena.domain.payment.dto.res.KakaoPayApproveResponse;
import goorm.athena.domain.payment.dto.res.KakaoPayReadyResponse;
import goorm.athena.domain.payment.entity.Payment;
import goorm.athena.domain.payment.entity.Status;
import goorm.athena.domain.payment.repository.PaymentRepository;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentCommandService {

    private final KakaoPayService kakaoPayService;
    private final OrderCommendService orderCommendService;
    private final OrderQueryService orderQueryService;
    private final PaymentRepository paymentRepository;


    public KakaoPayReadyResponse readyPayment(Long orderId) {

        Order order = orderQueryService.getById(orderId);
        User user = order.getUser();

        // 기존 Payment가 있는지 확인
        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
            // 이미 결제 완료된 건이라면 예외 처리
            if (payment.getStatus() == Status.APPROVED) {
                throw new CustomException(ErrorCode.ALREADY_PAYMENT_COMPLETED);
            }

            // 기존 결제 있으면 삭제
            paymentRepository.delete(payment);
            paymentRepository.flush();
        });

        // 카카오 결제 요청
        PaymentReadyRequest requestDto = PaymentReadyRequest.from(order);
        KakaoPayReadyResponse response;
        try {
            response = kakaoPayService
                    .requestKakaoPayment(requestDto, user, orderId);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.KAKAO_PAY_UNAVAILABLE);
        }

        Payment payment = Payment.create(order, user, response.tid(), order.getTotalPrice());
        paymentRepository.save(payment);

        return response;
    }


    public KakaoPayApproveResponse approvePayment(String pgToken, Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        User user = payment.getUser();
        PaymentApproveRequest requestDto = new PaymentApproveRequest(orderId, pgToken);

        KakaoPayApproveResponse response;

        try {
            response = kakaoPayService.approveKakaoPayment(payment.getTid(), requestDto, user);
        } catch (CustomException ce) {
            log.warn("카카오페이 결제 승인 중 예외 발생: {}", ce.getMessage());
            return KakaoPayApproveResponse.ofFailure("카카오페이 연동 실패 발생");
        };

        if (response.tid() == null) {
            log.warn("카카오 결제 승인 내부 응답값이 올바르지 않음");
            return KakaoPayApproveResponse.ofFailure("카카오 결제 승인 내부 응답값이 올바르지 않음");
        }


        try {
            payment.approve(pgToken);

//            orderCommendService.decreaseInventory(payment.getOrder().getId()); // 재고 감소
//            orderCommendService.increaseProjectFunding(orderId); // 누적 가격 증가
            orderCommendService.postPaymentProcess(orderId); // 재고 감소 ,누적 가격 증가

            return response;

        } catch (Exception e) {
            log.error("결제 승인 후 내부 처리 오류", e);
            return KakaoPayApproveResponse.ofFailure("걀제 승인 후 재고,누적 처리 오류");
        }


//        KakaoPayApproveResponse response;
//        try {
//            response = kakaoPayService
//                    .approveKakaoPayment(payment.getTid(), requestDto, user);
//        } catch (Exception e) {
//            log.error(" 카카오 결제 승인 실패", e);
//            return KakaoPayApproveResponse.ofFailure();
//        }

//        payment.approve(pgToken);
//
//        List<OrderItem> orderItems = orderItemRepository.findByOrderId(payment.getOrder().getId());
//        for (OrderItem item : orderItems) {
//            item.getProduct().decreaseStock(item.getQuantity());
//            item.getOrder().getProject().increasePrice(item.getPrice());
//        }
//        return KakaoPayApproveResponse.ofSuccess(response);
//    }
    }
}
