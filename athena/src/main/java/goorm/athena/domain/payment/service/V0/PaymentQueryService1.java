package goorm.athena.domain.payment.service.V0;

import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.payment.entity.Payment;
import goorm.athena.domain.payment.repository.PaymentQueryRepository;
import goorm.athena.domain.payment.repository.PaymentRepository1;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentQueryService1 {

    private final PaymentQueryRepository paymentQueryRepository;
    private final PaymentRepository1 paymentRepository1;

    public List<Order> getUnsettledOrdersByProjects(List<Project> projects) {
        return paymentQueryRepository.findUnsettledOrdersByProjects(projects);
    }

    public Payment findByOrderId(Long orderId) {
        return paymentRepository1.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
    }

}
