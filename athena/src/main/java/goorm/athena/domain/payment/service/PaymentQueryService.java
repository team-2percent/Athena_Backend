package goorm.athena.domain.payment.service;

import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.payment.repository.PaymentQueryRepository;
import goorm.athena.domain.project.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentQueryService {

    private final PaymentQueryRepository paymentQueryRepository;

    public List<Order> getUnsettledOrdersByProjects(List<Project> projects) {
        return paymentQueryRepository.findUnsettledOrdersByProjects(projects);
    }
}
