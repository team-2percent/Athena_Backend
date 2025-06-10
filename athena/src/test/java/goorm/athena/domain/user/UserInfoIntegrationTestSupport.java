package goorm.athena.domain.user;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.bankaccount.repository.BankAccountRepository;
import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.category.repository.CategoryRepository;
import goorm.athena.domain.comment.entity.Comment;
import goorm.athena.domain.comment.repository.CommentRepository;
import goorm.athena.domain.comment.service.CommentService;
import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.coupon.repository.CouponRepository;
import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;
import goorm.athena.domain.deliveryinfo.repository.DeliveryInfoRepository;
import goorm.athena.domain.image.service.ImageService;
import goorm.athena.domain.image.service.NasService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.order.repository.OrderRepository;
import goorm.athena.domain.orderitem.entity.OrderItem;
import goorm.athena.domain.orderitem.repository.OrderItemRepository;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.product.repository.ProductRepository;
import goorm.athena.domain.project.entity.PlatformPlan;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.repository.PlatformPlanRepository;
import goorm.athena.domain.project.repository.ProjectRepository;
import goorm.athena.domain.project.service.ProjectService;
import goorm.athena.domain.user.controller.UserInfoControllerImpl;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.MyInfoQueryRepository;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.domain.user.service.MyInfoService;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.domain.userCoupon.entity.Status;
import goorm.athena.domain.userCoupon.entity.UserCoupon;
import goorm.athena.domain.userCoupon.repository.UserCouponCursorRepository;
import goorm.athena.domain.userCoupon.repository.UserCouponRepository;
import goorm.athena.domain.userCoupon.service.UserCouponQueryService;
import goorm.athena.util.IntegrationControllerTestSupport;
import goorm.athena.util.TestEntityFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.LocalDateTime;

public abstract class UserInfoIntegrationTestSupport extends IntegrationControllerTestSupport {
    protected UserInfoControllerImpl controller;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected UserService userService;

    @Autowired
    protected CommentService commentService;

    @Autowired
    protected MyInfoService myInfoService;

    @Autowired
    protected UserCouponQueryService userCouponQueryService;

    @Autowired
    protected ImageGroupService imageGroupService;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected BankAccountRepository bankAccountRepository;

    @Autowired
    protected PlatformPlanRepository platformPlanRepository;

    @Autowired
    protected NasService nasService;

    @Autowired
    protected ProjectRepository projectRepository;

    @Autowired
    protected ProjectService projectService;

    @Autowired
    protected ImageService imageService;

    @Autowired
    protected CommentRepository commentRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected OrderItemRepository orderItemRepository;

    @Autowired
    protected OrderRepository orderRepository;

    @Autowired
    protected DeliveryInfoRepository deliveryInfoRepository;

    @Autowired
    protected ProductRepository productRepository;

    @TempDir
    Path tempDir;

    @Autowired
    protected MyInfoQueryRepository myInfoQueryRepository;

    @Autowired
    protected CouponRepository couponRepository;

    @Autowired
    protected UserCouponRepository userCouponRepository;

    @Autowired
    protected UserCouponCursorRepository userCouponCursorRepository;

    @BeforeEach
    void setUp() {
        controller = new UserInfoControllerImpl(commentService, myInfoService, userService, userCouponQueryService);
        Field imagePathField = ReflectionUtils.findField(NasService.class, "imagePath");
        imagePathField.setAccessible(true);
        ReflectionUtils.setField(imagePathField, nasService, tempDir.toAbsolutePath().toString());
    }

    protected ImageGroup setupUserImageGroup() {
        return imageGroupService.createImageGroup(Type.USER);
    }

    protected ImageGroup setupProjectImageGroup() {
        return imageGroupService.createImageGroup(Type.PROJECT);
    }


    protected User setupUser(String email, String password, String nickname, ImageGroup imageGroup) {
        User user = TestEntityFactory.createUser(email, password, nickname, imageGroup, Role.ROLE_USER);
        return user;
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

    protected Category setupCategory(String categoryName) {
        Category category = TestEntityFactory.createCategory(categoryName);
        return category;
    }

    protected BankAccount setupBankAccount(User user, String accountNumber, String accountHolder, String bankName, boolean isDefault) {
        BankAccount bankAccount = TestEntityFactory.createBankAccount(user, accountNumber, accountHolder, bankName, isDefault);
        return bankAccount;
    }

    protected Product setupProduct(Project project, String name, String description, Long price, Long stock){
        Product product = TestEntityFactory.createProduct(
                project, name, description, price, stock);

        return product;
    }

    protected Comment setupComment(User user, Project project, String content){
        return Comment.builder()
                .user(user)
                .project(project)
                .content(content)
                .build();
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

    protected Coupon setupCoupon(String title, String content, int price, LocalDateTime startAt,
                                 LocalDateTime endAt, LocalDateTime expiresAt, int stock, CouponStatus couponStatus){
        return TestEntityFactory.createCoupon(title, content, price, startAt, endAt, expiresAt, stock, couponStatus);
    }

    protected UserCoupon setupUserCoupon(User user, Coupon coupon, Status status){
        return TestEntityFactory.createUserCoupon(user, coupon, status);
    }
}
