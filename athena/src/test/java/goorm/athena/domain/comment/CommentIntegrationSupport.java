package goorm.athena.domain.comment;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.bankaccount.repository.BankAccountRepository;
import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.category.repository.CategoryRepository;
import goorm.athena.domain.comment.entity.Comment;
import goorm.athena.domain.comment.repository.CommentRepository;
import goorm.athena.domain.comment.service.CommentCommandService;
import goorm.athena.domain.comment.service.CommentQueryService;
import goorm.athena.domain.image.service.ImageService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.project.entity.PlatformPlan;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.repository.PlatformPlanRepository;
import goorm.athena.domain.project.repository.ProjectRepository;
import goorm.athena.domain.project.service.ProjectService;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.util.IntegrationServiceTestSupport;
import goorm.athena.util.TestEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class CommentIntegrationSupport extends IntegrationServiceTestSupport {
    @Autowired
    protected CommentRepository commentRepository;

    @Autowired
    protected ImageGroupService imageGroupService;

    @Autowired
    protected CommentQueryService commentQueryService;

    @Autowired
    protected CommentCommandService commentCommandService;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected BankAccountRepository bankAccountRepository;

    @Autowired
    protected PlatformPlanRepository platformPlanRepository;

    @Autowired
    protected ProjectRepository projectRepository;

    @Autowired
    protected ProjectService projectService;

    @Autowired
    protected ImageService imageService;

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

    protected Project setupProject(User user, Category category, ImageGroup imageGroup,
                                   BankAccount bankAccount, PlatformPlan platformPlan,
                                   String title, String description, Long goalAmount, Long totalAmount, String contentMarkdown) {
        Project project = TestEntityFactory.createProject(
                user, category, imageGroup, bankAccount, platformPlan,
                title, description, goalAmount, totalAmount, contentMarkdown
        );
        return project;
    }

    protected Comment setupComment(User user, Project project, String content){
        return TestEntityFactory.createComment(user, project, content);
    }
}
