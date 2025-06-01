package goorm.athena.domain.user;

import goorm.athena.domain.image.service.ImageService;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.notification.service.FcmTokenService;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.domain.user.service.MyInfoService;
import goorm.athena.domain.user.service.TokenService;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.global.jwt.util.JwtTokenizer;
import goorm.athena.util.IntegrationTestSupport;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

public abstract class UserIntegrationTestSupport extends IntegrationTestSupport {
  @Mock
  protected UserRepository userRepository;

  @Mock
  protected ImageService imageService;

  @Mock
  protected JwtTokenizer jwtTokenizer;

  @Mock
  protected TokenService tokenService;

  @Mock
  protected PasswordEncoder passwordEncoder;

  @Mock
  protected HttpServletResponse httpServletResponse;

  @InjectMocks
  protected UserService userService;

  protected static Validator validator;

  @BeforeAll
  static void setupValidator() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

}
