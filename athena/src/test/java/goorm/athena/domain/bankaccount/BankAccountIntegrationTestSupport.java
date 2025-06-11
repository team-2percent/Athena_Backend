package goorm.athena.domain.bankaccount;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.bankaccount.repository.BankAccountRepository;
import goorm.athena.domain.bankaccount.service.BankAccountCommandService;
import goorm.athena.domain.bankaccount.service.BankAccountQueryService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.repository.ImageGroupRepository;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.order.repository.OrderRepository;
import goorm.athena.domain.orderitem.repository.OrderItemRepository;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.product.repository.ProductRepository;
import goorm.athena.domain.project.repository.ProjectRepository;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.util.IntegrationServiceTestSupport;
import goorm.athena.util.TestEntityFactory;
import org.junit.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.SQLException;

public abstract class BankAccountIntegrationTestSupport extends IntegrationServiceTestSupport {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ImageGroupService imageGroupService;

    @Autowired
    protected BankAccountRepository bankAccountRepository;

    @Autowired
    protected BankAccountQueryService bankAccountQueryService;

    @Autowired
    protected BankAccountCommandService bankAccountCommandService;


    @Autowired
    protected ImageGroupRepository imageGroupRepository;

    protected ImageGroup setupImageGroup() {
        return imageGroupService.createImageGroup(Type.USER);
    }
    @Autowired protected DataSource dataSource;
    @Autowired protected ResourceLoader resourceLoader;

    @BeforeEach
    void setup() throws SQLException {
        ScriptUtils.executeSqlScript(
                dataSource.getConnection(),
                resourceLoader.getResource("classpath:/truncate.sql")
        );
        // 2. SQL 스크립트 실행 (예: user-test-data.sql)
        ScriptUtils.executeSqlScript(
                dataSource.getConnection(),
                resourceLoader.getResource("classpath:/data1.sql")
        );
    }



    protected User setupUser(String email, String password, String nickname, ImageGroup imageGroup) {
        User user = TestEntityFactory.createUser(email, password, nickname, imageGroup, Role.ROLE_USER);
        return user;
    }

    protected BankAccount setupBankAccount(User user, String accountNumber, String accountHolder, String bankName, boolean isDefault){
        return BankAccount.builder()
                .user(user)
                .accountNumber(accountNumber)
                .accountHolder(accountHolder)
                .bankName(bankName)
                .isDefault(isDefault)
                .build();
    }
}
