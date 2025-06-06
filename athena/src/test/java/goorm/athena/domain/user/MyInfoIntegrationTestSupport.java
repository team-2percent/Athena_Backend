package goorm.athena.domain.user;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.bankaccount.repository.BankAccountRepository;
import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.category.repository.CategoryRepository;
import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;
import goorm.athena.domain.deliveryinfo.repository.DeliveryInfoRepository;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.repository.ImageGroupRepository;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.order.repository.OrderRepository;
import goorm.athena.domain.orderitem.entity.OrderItem;
import goorm.athena.domain.orderitem.repository.OrderItemRepository;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.product.repository.ProductRepository;
import goorm.athena.domain.project.entity.PlanName;
import goorm.athena.domain.project.entity.PlatformPlan;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.repository.PlatformPlanRepository;
import goorm.athena.domain.project.repository.ProjectRepository;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.domain.user.service.MyInfoService;
import goorm.athena.util.IntegrationServiceTestSupport;
import goorm.athena.util.TestEntityFactory;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public abstract class MyInfoIntegrationTestSupport extends IntegrationServiceTestSupport {

    @Autowired
    protected MyInfoService myInfoService;

    @Autowired
    protected ProjectRepository projectRepository;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ImageGroupService imageGroupService;

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

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        deliveryInfoRepository.deleteAllInBatch();
        projectRepository.deleteAllInBatch();
        platformPlanRepository.deleteAllInBatch();
        bankAccountRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        imageGroupRepository.deleteAllInBatch();
    }

    protected ImageGroup setupImageGroup() {
        return imageGroupService.createImageGroup(Type.PROJECT);
    }

    protected User setupUser(String email, String password, String nickname, ImageGroup imageGroup) {
        User user = TestEntityFactory.createUser(email, password, nickname, imageGroup, Role.ROLE_USER);
        return user;
    }

    protected Category setupCategory(String categoryName) {
        Category category = TestEntityFactory.createCategory(categoryName);
        return category;
    }

    protected BankAccount setupBankAccount(User user, String accountNumber, String accountHolder, String bankName, boolean isDefault) {
        BankAccount bankAccount = TestEntityFactory.createBankAccount(user, accountNumber, accountHolder, bankName, isDefault);
        return bankAccount;
    }

    protected PlatformPlan setupPlatformPlan(PlanName planName, int platformFeeRate, int pgFeeRate, int vatRate, String description) {
        PlatformPlan plan = TestEntityFactory.createPlatformPlan(planName, platformFeeRate, pgFeeRate, vatRate, description);
        return plan;
    }

    protected Project setupProject(User user, Category category, ImageGroup imageGroup,
                                 BankAccount bankAccount, PlatformPlan platformPlan,
                                   String title, String description, Long goalAmount, Long totalAmount, String contentMarkdown) {
        Project project = TestEntityFactory.createProject(
                user, category, imageGroup, bankAccount, platformPlan,
                title, description, goalAmount, totalAmount, contentMarkdown
        );
        return project;
    }

    protected Product setupProduct(Project project, String name, String description, Long price, Long stock){
        Product product = TestEntityFactory.createProduct(
                project, name, description, price, stock);

        return product;
    }

    protected DeliveryInfo setupDeliveryInfo(User user, String zipcode, String address, String detailAddress, boolean isDefault){
        DeliveryInfo deliveryInfo = TestEntityFactory.createDeliveryInfo(user, zipcode, address, detailAddress, isDefault);

        return deliveryInfo;
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
