package goorm.athena.domain.bankaccount;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.bankaccount.repository.BankAccountRepository;
import goorm.athena.domain.bankaccount.service.BankAccountCommandService;
import goorm.athena.domain.bankaccount.service.BankAccountQueryService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.service.ImageGroupCommandService;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.util.IntegrationServiceTestSupport;
import goorm.athena.util.TestEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BankAccountIntegrationTestSupport extends IntegrationServiceTestSupport {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ImageGroupCommandService imageGroupCommandService;

    @Autowired
    protected BankAccountRepository bankAccountRepository;

    @Autowired
    protected BankAccountQueryService bankAccountQueryService;

    @Autowired
    protected BankAccountCommandService bankAccountCommandService;


    protected ImageGroup setupImageGroup() {
        return imageGroupCommandService.createImageGroup(Type.USER);
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
