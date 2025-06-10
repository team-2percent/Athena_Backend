package goorm.athena.domain.bankaccount;

import goorm.athena.domain.bankaccount.controller.BankAccountControllerImpl;
import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.bankaccount.repository.BankAccountRepository;
import goorm.athena.domain.bankaccount.service.BankAccountCommandService;
import goorm.athena.domain.bankaccount.service.BankAccountQueryService;
import goorm.athena.domain.image.service.ImageService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.notification.service.FcmTokenService;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.domain.user.service.UserQueryService;
import goorm.athena.domain.userCoupon.service.UserCouponQueryService;
import goorm.athena.global.jwt.util.LoginUserRequest;
import goorm.athena.util.IntegrationControllerTestSupport;
import goorm.athena.util.TestEntityFactory;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BankAccountControllerIntegrationTestSupport extends IntegrationControllerTestSupport {
    protected BankAccountControllerImpl controller;
    protected LoginUserRequest loginUserRequest;

    @Autowired
    protected BankAccountCommandService bankAccountCommandService;

    @Autowired
    protected BankAccountQueryService bankAccountQueryService;

    @Autowired
    protected BankAccountRepository bankAccountRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ImageGroupService imageGroupService;

    @Autowired
    protected UserCouponQueryService userCouponQueryService;

    @Autowired
    protected FcmTokenService fcmTokenService;

    @Autowired
    protected ImageService imageService;

    @Autowired
    protected UserQueryService userQueryService;

    @BeforeEach
    void setUp() {
        controller = new BankAccountControllerImpl(bankAccountQueryService, bankAccountCommandService);
        loginUserRequest = new LoginUserRequest("123", 1L, Role.ROLE_USER);
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
