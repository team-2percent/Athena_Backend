package goorm.athena.domain.settlement;

import goorm.athena.domain.order.repository.OrderRepository;
import goorm.athena.domain.payment.service.PaymentQueryService;
import goorm.athena.domain.project.service.ProjectService;
import goorm.athena.domain.settlement.repository.SettlementRepository;
import goorm.athena.domain.settlement.service.SettlementCommandService;
import goorm.athena.util.IntegrationServiceTestSupport;
import org.springframework.beans.factory.annotation.Autowired;

public class SettlementIntegrationTestSupport extends IntegrationServiceTestSupport {

    @Autowired protected SettlementCommandService settlementCommandService;
    @Autowired protected SettlementRepository settlementRepository;
    @Autowired protected ProjectService projectService;
    @Autowired protected OrderRepository orderRepository;
    @Autowired protected PaymentQueryService paymentQueryService;
}
