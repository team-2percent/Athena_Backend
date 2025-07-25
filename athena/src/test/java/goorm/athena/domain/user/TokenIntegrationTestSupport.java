package goorm.athena.domain.user;

import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.domain.user.service.TokenCommandService;
import goorm.athena.util.IntegrationServiceTestSupport;
import goorm.athena.util.TestEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class TokenIntegrationTestSupport extends IntegrationServiceTestSupport {

    @Autowired
    protected TokenCommandService tokenCommandService;

    @Autowired
    protected UserRepository userRepository;

    protected User setupUser(String email, String password, String nickname, ImageGroup imageGroup) {
        User user = TestEntityFactory.createUser(email, password, nickname, imageGroup, Role.ROLE_USER);
        return user;
    }
}
