package goorm.athena.global.jwt;

import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.domain.user.service.RefreshTokenService;
import goorm.athena.global.jwt.util.JwtTokenizer;
import goorm.athena.util.IntegrationServiceTestSupport;
import goorm.athena.util.TestEntityFactory;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;

public abstract class JwtTokenizerTestSupport extends IntegrationServiceTestSupport {
    @Autowired
    protected JwtTokenizer jwtTokenizer;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RefreshTokenService refreshTokenService;

    @Autowired
    protected MockHttpServletResponse response;


    @Value("${jwt.secretKey}")
    private String originalKey;

    @Value("${jwt.refreshKey}")
    private String refreshKey;

    private byte[] originalRefreshKey;
    private byte[] originalAccessKey;

    @BeforeEach
    void setUp() {
        originalRefreshKey = originalKey.getBytes(StandardCharsets.UTF_8);
        originalAccessKey = refreshKey.getBytes(StandardCharsets.UTF_8);

        ReflectionTestUtils.setField(jwtTokenizer, "refreshSecret", originalRefreshKey);
        ReflectionTestUtils.setField(jwtTokenizer, "accessSecret", originalAccessKey);
    }

    protected User setupUser(String email, String password, String nickname, ImageGroup imageGroup) {
        User user = TestEntityFactory.createUser(email, password, nickname, imageGroup);
        return user;
    }

}
