package goorm.athena.domain.order;

import goorm.athena.domain.bankaccount.repository.BankAccountRepository;
import goorm.athena.domain.category.repository.CategoryRepository;
import goorm.athena.domain.deliveryinfo.repository.DeliveryInfoRepository;
import goorm.athena.domain.imageGroup.repository.ImageGroupRepository;
import goorm.athena.domain.order.repository.OrderRepository;
import goorm.athena.domain.order.service.OrderCommendService;
import goorm.athena.domain.order.service.OrderQueryService;
import goorm.athena.domain.product.repository.ProductRepository;
import goorm.athena.domain.project.repository.PlatformPlanRepository;
import goorm.athena.domain.project.repository.ProjectRepository;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.util.IntegrationServiceTestSupport;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class OrderIntergrationTestSupport extends IntegrationServiceTestSupport {

    @Autowired
    protected OrderCommendService orderCommendService;

    @Autowired protected UserRepository userRepository;
    @Autowired protected DeliveryInfoRepository deliveryInfoRepository;
    @Autowired protected ProductRepository productRepository;
    @Autowired protected ProjectRepository projectRepository;
    @Autowired protected PlatformPlanRepository platformPlanRepository;
    @Autowired protected ImageGroupRepository imageGroupRepository;
    @Autowired protected CategoryRepository categoryRepository;
    @Autowired protected BankAccountRepository bankAccountRepository;
    @Autowired protected OrderRepository orderRepository;
    @Autowired protected OrderQueryService orderQueryService;



}
