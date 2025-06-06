package goorm.athena.domain.bankaccount;

import goorm.athena.domain.bankaccount.controller.BankAccountControllerImpl;
import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.bankaccount.repository.BankAccountRepository;
import goorm.athena.domain.bankaccount.service.BankAccountService;
import goorm.athena.domain.image.service.ImageService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.notification.service.FcmTokenService;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.domain.userCoupon.service.UserCouponService;
import goorm.athena.global.jwt.util.LoginUserRequest;
import goorm.athena.util.IntegrationControllerTestSupport;
import goorm.athena.util.TestEntityFactory;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BankAccountControllerIntegrationTestSupport extends IntegrationControllerTestSupport {
    protected BankAccountControllerImpl controller;
    protected LoginUserRequest loginUserRequest;

    @Autowired
    protected BankAccountService bankAccountService;

    @Autowired
    protected BankAccountRepository bankAccountRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ImageGroupService imageGroupService;

    @Autowired
    protected UserCouponService userCouponService;

    @Autowired
    protected FcmTokenService fcmTokenService;

    @Autowired
    protected ImageService imageService;

    @Autowired
    protected UserService userService;

    @BeforeEach
    void setUp() {
        controller = new BankAccountControllerImpl(bankAccountService);
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
