package goorm.athena.domain.user;

import goorm.athena.domain.user.service.TokenService;
import goorm.athena.global.jwt.util.JwtTokenizer;
import goorm.athena.util.IntegrationTestSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public abstract class TokenIntegrationTestSupport extends IntegrationTestSupport {
    @Mock
    protected JwtTokenizer jwtTokenizer;

    @Mock
    protected HttpServletResponse response;

    @InjectMocks
    protected TokenService tokenService;
}
