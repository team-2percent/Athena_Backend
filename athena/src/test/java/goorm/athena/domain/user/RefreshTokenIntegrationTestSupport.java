package goorm.athena.domain.user;

import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.service.ImageGroupCommandService;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.domain.user.service.RefreshTokenCommandService;
import goorm.athena.global.jwt.util.JwtTokenizer;
import goorm.athena.util.IntegrationServiceTestSupport;
import goorm.athena.util.TestEntityFactory;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class RefreshTokenIntegrationTestSupport extends IntegrationServiceTestSupport {

    @Autowired
    protected JwtTokenizer jwtTokenizer;

    @Autowired
    protected HttpServletResponse response;

    @Autowired
    protected RefreshTokenCommandService refreshTokenCommandService;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ImageGroupCommandService imageGroupCommandService;

    protected ImageGroup setupImageGroup() {
        return imageGroupCommandService.createImageGroup(Type.PROJECT);
    }

    protected User setupUser(String email, String password, String nickname, ImageGroup imageGroup) {
        User user = TestEntityFactory.createUser(email, password, nickname, imageGroup, Role.ROLE_USER);
        return user;
    }
}
