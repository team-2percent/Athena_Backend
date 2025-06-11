package goorm.athena.domain.comment;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.bankaccount.repository.BankAccountRepository;
import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.category.repository.CategoryRepository;
import goorm.athena.domain.comment.controller.CommentControllerImpl;
import goorm.athena.domain.comment.entity.Comment;
import goorm.athena.domain.comment.repository.CommentRepository;
import goorm.athena.domain.comment.service.CommentService;
import goorm.athena.domain.image.repository.ImageRepository;
import goorm.athena.domain.image.service.ImageQueryService;
import goorm.athena.domain.image.service.NasService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.service.ImageGroupCommandService;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.product.repository.ProductRepository;
import goorm.athena.domain.project.entity.PlatformPlan;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.repository.PlatformPlanRepository;
import goorm.athena.domain.project.repository.ProjectRepository;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.global.jwt.util.LoginUserRequest;
import goorm.athena.util.IntegrationControllerTestSupport;
import goorm.athena.util.TestEntityFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.nio.file.Path;

public abstract class CommentControllerIntegrationSupport extends IntegrationControllerTestSupport {
    @Autowired
    protected CommentControllerImpl controller;
    protected LoginUserRequest loginUserRequest;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ImageGroupCommandService imageGroupCommandService;

    @Autowired
    protected ImageQueryService imageQueryService;

    @Autowired
    protected NasService nasService;

    @TempDir
    Path tempDir;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected PlatformPlanRepository platformPlanRepository;

    @Autowired
    protected BankAccountRepository bankAccountRepository;

    @Autowired
    protected ProjectRepository projectRepository;

    @Autowired
    protected CommentRepository commentRepository;

    @Autowired
    protected CommentService commentService;

    @Autowired
    protected ImageRepository imageRepository;

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected UserService userService;

    @BeforeEach
    protected void setUp() {
        loginUserRequest = new LoginUserRequest("123", 1L, Role.ROLE_USER);
        // 테스트에서 nasService로 내부 경로를 강제 주입하여 임시 디렉터리로 파일 I/O 수행함
        Field imagePathField = ReflectionUtils.findField(NasService.class, "imagePath");
        imagePathField.setAccessible(true);
        ReflectionUtils.setField(imagePathField, nasService, tempDir.toAbsolutePath().toString());
    }

    protected User setupUser(String email, String password, String nickname, ImageGroup imageGroup) {
        User user = TestEntityFactory.createUser(email, password, nickname, imageGroup, Role.ROLE_USER);
        return user;
    }

    protected ImageGroup setupUserImageGroup() {
        return imageGroupCommandService.createImageGroup(Type.USER);
    }

    protected ImageGroup setupProjectImageGroup() {
        return imageGroupCommandService.createImageGroup(Type.PROJECT);
    }

    protected Category setupCategory(String categoryName) {
        Category category = TestEntityFactory.createCategory(categoryName);
        return category;
    }

    protected BankAccount setupBankAccount(User user, String accountNumber, String accountHolder, String bankName, boolean isDefault) {
        BankAccount bankAccount = TestEntityFactory.createBankAccount(user, accountNumber, accountHolder, bankName, isDefault);
        return bankAccount;
    }

    protected Project setupProject(User user, Category category, ImageGroup imageGroup,
                                   BankAccount bankAccount, PlatformPlan platformPlan,
                                   String title, String description, Long goalAmount, Long totalAmount, String contentMarkDown) {
        Project project = TestEntityFactory.createProject(
                user, category, imageGroup, bankAccount, platformPlan,
                title, description, goalAmount, totalAmount, contentMarkDown
        );
        return project;
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
}
