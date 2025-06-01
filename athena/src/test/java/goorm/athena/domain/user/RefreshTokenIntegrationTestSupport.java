package goorm.athena.domain.user;

import goorm.athena.domain.user.service.RefreshTokenService;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.global.jwt.util.JwtTokenizer;
import goorm.athena.util.IntegrationTestSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

public abstract class RefreshTokenIntegrationTestSupport extends IntegrationTestSupport {
    @Mock
    protected UserService userService;

    @Mock
    protected JwtTokenizer jwtTokenizer;

    @Mock
    protected HttpServletResponse response;

    @InjectMocks
    protected RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        response = new MockHttpServletResponse();
    }
}
