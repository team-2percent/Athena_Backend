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
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.mapper.MyProjectScrollResponseMapper;
import goorm.athena.domain.user.repository.MyInfoQueryRepository;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.domain.user.service.MyInfoService;
import goorm.athena.domain.user.service.RefreshTokenService;
import goorm.athena.domain.user.service.TokenService;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.global.jwt.util.JwtTokenizer;
import goorm.athena.util.IntegrationTestSupport;
import goorm.athena.util.TestEntityFactory;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AutoConfigureMockMvc
public abstract class RefreshTokenIntegrationTestSupport extends IntegrationTestSupport {

    @Autowired
    protected JwtTokenizer jwtTokenizer;

    @Autowired
    protected HttpServletResponse response;

    @Autowired
    protected RefreshTokenService refreshTokenService;

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
        User user = TestEntityFactory.createUser(email, password, nickname, imageGroup);
        return user;
    }
}
