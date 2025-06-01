package goorm.athena.domain.user;

import goorm.athena.domain.user.controller.RefreshControllerImpl;
import goorm.athena.domain.user.service.RefreshTokenService;
import goorm.athena.global.jwt.util.JwtTokenizer;
import goorm.athena.util.IntegrationTestSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public abstract class RefreshControllerIntegrationTestSupport extends IntegrationTestSupport {

    @InjectMocks
    protected RefreshControllerImpl refreshController;

    @Mock
    protected RefreshTokenService refreshTokenService;

    @Mock
    protected JwtTokenizer jwtTokenizer;

    @Mock
    protected HttpServletResponse httpServletResponse;
}
