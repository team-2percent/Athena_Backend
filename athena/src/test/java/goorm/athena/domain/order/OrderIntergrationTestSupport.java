package goorm.athena.domain.order;

import goorm.athena.domain.bankaccount.repository.BankAccountRepository;
import goorm.athena.domain.category.repository.CategoryRepository;
import goorm.athena.domain.deliveryinfo.repository.DeliveryInfoRepository;
import goorm.athena.domain.deliveryinfo.service.DeliveryInfoQueryService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.repository.ImageGroupRepository;
import goorm.athena.domain.order.repository.OrderRepository;
import goorm.athena.domain.order.service.OrderCommendService;
import goorm.athena.domain.order.service.OrderQueryService;
import goorm.athena.domain.order.service.OrderQueryServiceTest;
import goorm.athena.domain.orderitem.repository.OrderItemRepository;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.product.repository.ProductRepository;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.repository.PlatformPlanRepository;
import goorm.athena.domain.project.repository.ProjectRepository;
import goorm.athena.domain.project.service.ProjectService;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.domain.user.service.UserQueryService;
import goorm.athena.util.IntegrationServiceTestSupport;
import goorm.athena.util.TestEntityFactory;
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
    @Autowired protected OrderItemRepository orderItemRepository;

    @Autowired protected UserQueryService userQueryService;
    @Autowired protected DeliveryInfoQueryService deliveryInfoQueryService;
    @Autowired protected ProjectService projectService;





}
