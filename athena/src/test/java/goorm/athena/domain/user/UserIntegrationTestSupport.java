package goorm.athena.domain.user;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;
import goorm.athena.domain.image.repository.ImageRepository;
import goorm.athena.domain.image.service.ImageService;
import goorm.athena.domain.image.service.NasService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.repository.ImageGroupRepository;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.orderitem.entity.OrderItem;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.project.entity.PlanName;
import goorm.athena.domain.project.entity.PlatformPlan;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.global.jwt.util.JwtTokenizer;
import goorm.athena.util.IntegrationServiceTestSupport;

import goorm.athena.util.TestEntityFactory;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ReflectionUtils;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.LocalDateTime;

public abstract class UserIntegrationTestSupport extends IntegrationServiceTestSupport {
  @Autowired
  protected UserRepository userRepository;

  @Autowired
  protected ImageService imageService;

  @Autowired
  protected JwtTokenizer jwtTokenizer;

  @Autowired
  protected ImageRepository imageRepository;

  @Autowired
  protected NasService nasService;

  @TempDir
  Path tempDir;


  @Autowired
  protected ImageGroupRepository imageGroupRepository;

  @Autowired
  protected PasswordEncoder passwordEncoder;

  @Autowired
  protected HttpServletResponse httpServletResponse;

  @Autowired
  protected ImageGroupService imageGroupService;

  @Autowired
  protected UserService userService;

  @BeforeEach
  void setUp() {
    userRepository.deleteAllInBatch();
    imageGroupRepository.deleteAllInBatch();
    // 테스트에서 nasService로 내부 경로를 강제 주입하여 임시 디렉터리로 파일 I/O 수행함
    Field imagePathField = ReflectionUtils.findField(NasService.class, "imagePath");
    imagePathField.setAccessible(true);
    ReflectionUtils.setField(imagePathField, nasService, tempDir.toAbsolutePath().toString());
  }

  protected static Validator validator;

  @BeforeAll
  static void setupValidator() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  protected ImageGroup setupImageGroup() {
    return imageGroupService.createImageGroup(Type.USER);
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
