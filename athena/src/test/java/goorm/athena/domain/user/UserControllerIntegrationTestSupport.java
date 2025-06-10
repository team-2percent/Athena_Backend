package goorm.athena.domain.user;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.image.service.ImageService;
import goorm.athena.domain.image.service.NasService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.notification.service.FcmTokenService;
import goorm.athena.domain.user.controller.UserControllerImpl;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.domain.user.service.RefreshTokenCommandService;
import goorm.athena.domain.user.service.UserCommandService;
import goorm.athena.domain.user.service.UserQueryService;
import goorm.athena.global.jwt.util.JwtTokenizer;
import goorm.athena.util.IntegrationControllerTestSupport;
import goorm.athena.util.TestEntityFactory;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BindingResult;

import java.lang.reflect.Field;
import java.nio.file.Path;

public abstract class UserControllerIntegrationTestSupport extends IntegrationControllerTestSupport {
    protected UserControllerImpl controller;

    @Autowired
    protected UserQueryService userQueryService;

    @Autowired
    protected UserCommandService userCommandService;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected JwtTokenizer jwtTokenizer;

    @Autowired
    protected ImageGroupService imageGroupService;

    @Autowired
    protected ImageService imageService;

    @Autowired
    protected RefreshTokenCommandService refreshTokenCommandService;

    @Autowired
    protected HttpServletResponse httpServletResponse;

    protected BindingResult bindingResult;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected NasService nasService;

    @Autowired
    protected FcmTokenService fcmTokenService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        controller = new UserControllerImpl(userQueryService, userCommandService, refreshTokenCommandService, imageGroupService, fcmTokenService);
        Field imagePathField = ReflectionUtils.findField(NasService.class, "imagePath");
        imagePathField.setAccessible(true);
        ReflectionUtils.setField(imagePathField, nasService, tempDir.toAbsolutePath().toString());
    }

    protected ImageGroup setupImageGroup() {
        return imageGroupService.createImageGroup(Type.USER);
    }

    protected User setupUser(String email, String password, String nickname, ImageGroup imageGroup) {
        User user = TestEntityFactory.createUser(email, password, nickname, imageGroup, Role.ROLE_USER);
        return user;
    }

    protected BankAccount setupBankAccount(User user, String accountNumber, String accountHolder, String bankName, boolean isDefault) {
        BankAccount bankAccount = TestEntityFactory.createBankAccount(user, accountNumber, accountHolder, bankName, isDefault);
        return bankAccount;
    }
}
