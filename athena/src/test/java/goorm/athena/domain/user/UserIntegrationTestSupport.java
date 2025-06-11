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
import goorm.athena.domain.user.service.UserCommandService;
import goorm.athena.domain.user.service.UserQueryService;
import goorm.athena.global.jwt.util.JwtTokenizer;
import goorm.athena.global.jwt.util.LoginUserRequest;
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
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
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
  protected UserQueryService userQueryService;

  @Autowired
  protected UserCommandService userCommandService;

  protected static Validator validator;

  @BeforeEach
  protected void setUp() {
    // 테스트에서 nasService로 내부 경로를 강제 주입하여 임시 디렉터리로 파일 I/O 수행함
    Field imagePathField = ReflectionUtils.findField(NasService.class, "imagePath");
    imagePathField.setAccessible(true);
    ReflectionUtils.setField(imagePathField, nasService, tempDir.toAbsolutePath().toString());
  }

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

}
