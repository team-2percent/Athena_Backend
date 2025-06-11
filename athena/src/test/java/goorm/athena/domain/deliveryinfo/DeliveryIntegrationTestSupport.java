package goorm.athena.domain.deliveryinfo;

import goorm.athena.domain.deliveryinfo.repository.DeliveryInfoRepository;
import goorm.athena.domain.deliveryinfo.service.DeliveryInfoCommandService;
import goorm.athena.domain.deliveryinfo.service.DeliveryInfoQueryService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.service.ImageGroupCommandService;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.util.IntegrationServiceTestSupport;
import goorm.athena.util.TestEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class DeliveryIntegrationTestSupport extends IntegrationServiceTestSupport {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ImageGroupCommandService imageGroupCommandService;

    @Autowired
    protected DeliveryInfoRepository deliveryInfoRepository;

    @Autowired
    protected DeliveryInfoQueryService deliveryInfoQueryService;

    @Autowired
    protected DeliveryInfoCommandService deliveryInfoCommandService;

    protected ImageGroup setupImageGroup() {
        return imageGroupCommandService.createImageGroup(Type.USER);
    }

    protected User setupUser(String email, String password, String nickname, ImageGroup imageGroup) {
        User user = TestEntityFactory.createUser(email, password, nickname, imageGroup, Role.ROLE_USER);
        return user;
    }
}
