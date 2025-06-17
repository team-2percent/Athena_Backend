package goorm.athena.domain.project.util;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.bankaccount.service.BankAccountQueryService;
import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.category.repository.CategoryRepository;
import goorm.athena.domain.category.service.CategoryService;
import goorm.athena.domain.image.entity.Image;
import goorm.athena.domain.image.service.ImageQueryService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.service.ImageGroupCommandService;
import goorm.athena.domain.imageGroup.service.ImageGroupQueryService;
import goorm.athena.domain.product.dto.req.ProductRequest;
import goorm.athena.domain.product.service.ProductQueryService;
import goorm.athena.domain.project.dto.req.ProjectCreateRequest;
import goorm.athena.domain.project.entity.PlanName;
import goorm.athena.domain.project.entity.PlatformPlan;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.repository.PlatformPlanRepository;
import goorm.athena.domain.project.service.ProjectCommandService;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.service.UserQueryService;
import goorm.athena.util.IntegrationServiceTestSupport;
import goorm.athena.util.TestEntityFactory;
import jakarta.transaction.Transactional;

import goorm.athena.domain.project.service.ProjectQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
public abstract class ProjectIntegrationTestSupport extends IntegrationServiceTestSupport {

    @Autowired
    protected ProjectQueryService projectQueryService;

    @Autowired
    protected ProjectCommandService projectCommandService;

    @Autowired
    protected CategoryService categoryService;

    @Autowired
    protected ImageQueryService imageQueryService;

    @Autowired
    protected ProductQueryService productQueryService;

    @Autowired
    protected ImageGroupQueryService imageGroupQueryService;

    @Autowired
    private UserQueryService userQueryService;

    @Autowired
    private ImageGroupCommandService imageGroupCommandService;

    @Autowired
    private BankAccountQueryService bankAccountQueryService;

    @Autowired
    private PlatformPlanRepository platformPlanRepository;

    /***
     * 프로젝트 테스트 시, data1.sql의 User(id=1)를 사용합니다.
     ***/

    protected ProjectCreateRequest createProjectRequest(String title, String description, String contentMarkDown, LocalDateTime startAt){
        ImageGroup imageGroup = setupImageGroup();
        List<ProductRequest> productRequests = List.of(new ProductRequest("테스트 상품", "상품 설명", 10000L, 100L, List.of("옵션1")));

        return new ProjectCreateRequest(1L, 1L, imageGroup.getId(), 1L,
                                        title, description, 100000L, contentMarkDown,
                                        startAt, startAt.plusDays(30), startAt.plusDays(60),
                            "BASIC", productRequests);
    }

    protected MultipartFile createMockFile(String filename, String contentType) {
        return new MockMultipartFile(filename, filename, contentType, "dummy_image".getBytes());
    }

    protected Project setupProject(String title, String description, long goalAmount, long totalAmount,
                                   String contentMarkDown){
        User user = userQueryService.getUser(1L);
        Category category = categoryService.getCategoryById(1L);
        BankAccount bankAccount = bankAccountQueryService.getBankAccount(1L);
        PlatformPlan platformPlan = platformPlanRepository.findByName(PlanName.BASIC);
        ImageGroup imageGroup = setupImageGroup();

        return TestEntityFactory.createProject(user, category, imageGroup, bankAccount, platformPlan, title, description, goalAmount, totalAmount, contentMarkDown);
    }

    private ImageGroup setupImageGroup(){
        return imageGroupCommandService.createImageGroup(Type.PROJECT);
    }

}
