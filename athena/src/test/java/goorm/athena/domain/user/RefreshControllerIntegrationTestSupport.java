package goorm.athena.domain.user;

import goorm.athena.domain.user.controller.RefreshControllerImpl;
import goorm.athena.domain.user.service.RefreshTokenService;
import goorm.athena.global.jwt.util.JwtTokenizer;
import goorm.athena.util.IntegrationTestSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

public abstract class RefreshControllerIntegrationTestSupport extends IntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected RefreshTokenService refreshTokenService;

    @MockBean
    protected JwtTokenizer jwtTokenizer;
}
