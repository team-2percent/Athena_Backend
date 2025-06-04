package goorm.athena.domain.user;

import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.service.TokenService;
import goorm.athena.util.IntegrationServiceTestSupport;
import goorm.athena.util.TestEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class TokenIntegrationTestSupport extends IntegrationServiceTestSupport {

    @Autowired
    protected TokenService tokenService;

    @Autowired
    protected ImageGroupService imageGroupService;

    protected ImageGroup setupImageGroup() {
        return imageGroupService.createImageGroup(Type.PROJECT);
    }

    protected User setupUser(String email, String password, String nickname, ImageGroup imageGroup) {
        User user = TestEntityFactory.createUser(email, password, nickname, imageGroup);
        return user;
    }
}
