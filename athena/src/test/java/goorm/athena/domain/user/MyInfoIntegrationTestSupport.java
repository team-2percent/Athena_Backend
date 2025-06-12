package goorm.athena.domain.user;

import goorm.athena.domain.bankaccount.repository.BankAccountRepository;
import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;
import goorm.athena.domain.deliveryinfo.repository.DeliveryInfoRepository;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.repository.ImageGroupRepository;
import goorm.athena.domain.imageGroup.service.ImageGroupCommandService;
import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.order.repository.OrderRepository;
import goorm.athena.domain.orderitem.entity.OrderItem;
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
import goorm.athena.domain.user.service.MyInfoQueryService;
import goorm.athena.util.IntegrationServiceTestSupport;
import goorm.athena.util.TestEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public abstract class MyInfoIntegrationTestSupport extends IntegrationServiceTestSupport {

    @Autowired
    protected MyInfoQueryService myInfoQueryService;

    @Autowired
    protected ProjectService projectService;

    @Autowired
    protected ProjectRepository projectRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ImageGroupCommandService imageGroupCommandService;

    @Autowired
    protected ImageGroupRepository imageGroupRepository;

    @Autowired
    protected BankAccountRepository bankAccountRepository;

    @Autowired
    protected PlatformPlanRepository platformPlanRepository;

    @Autowired
    protected OrderItemRepository orderItemRepository;

    @Autowired
    protected OrderRepository orderRepository;

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected DeliveryInfoRepository deliveryInfoRepository;

    protected ImageGroup setupImageGroup() {
        return imageGroupCommandService.createImageGroup(Type.PROJECT);
    }

    protected User setupUser(String email, String password, String nickname, ImageGroup imageGroup) {
        User user = TestEntityFactory.createUser(email, password, nickname, imageGroup, Role.ROLE_USER);
        return user;
    }

    protected OrderItem setupOrderItem(Order order, Product product, int quantity, Long price){
        OrderItem orderItem = TestEntityFactory.createOrderItem(order, product, quantity, price);

        return orderItem;
    }

    protected Order setupOrder(User user, DeliveryInfo delivery, Project project, LocalDateTime orderedAt){
        Order order = TestEntityFactory.createOrder(user, delivery, project, orderedAt);

        return order;
    }
}
