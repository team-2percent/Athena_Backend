package goorm.athena.domain.payment;

import goorm.athena.domain.order.service.OrderService;
import goorm.athena.domain.orderitem.repository.OrderItemRepository;
import goorm.athena.domain.payment.repository.PaymentRepository;
import goorm.athena.domain.payment.service.KakaoPayService;
import goorm.athena.domain.payment.service.PaymentService;
import goorm.athena.util.IntegrationServiceTestSupport;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class PaymentIntegrationTestSupport extends IntegrationServiceTestSupport {

    @Autowired
    protected PaymentService paymentService;

    @Autowired
    protected KakaoPayService kakaoPayService;

    @Autowired
    protected OrderService orderService;

    @Autowired
    protected PaymentRepository paymentRepository;

    @Autowired
    protected OrderItemRepository orderItemRepository;

}