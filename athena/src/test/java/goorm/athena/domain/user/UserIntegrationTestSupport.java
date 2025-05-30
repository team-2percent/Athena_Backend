package goorm.athena.domain.user;

import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.util.IntegrationTestSupport;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@ExtendWith(MockitoExtension.class)
public abstract class UserIntegrationTestSupport extends IntegrationTestSupport {
  @Mock
  protected UserRepository userRepository;

  @InjectMocks
  protected UserService userService;

  protected static Validator validator;

  @BeforeAll
  static void setupValidator() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

}
