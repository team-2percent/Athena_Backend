package goorm.athena.domain.deliveryinfo;

import goorm.athena.domain.deliveryinfo.repository.DeliveryInfoRepository;
import goorm.athena.domain.deliveryinfo.service.DeliveryInfoService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.util.IntegrationServiceTestSupport;
import goorm.athena.util.TestEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class DeliveryIntegrationTestSupport extends IntegrationServiceTestSupport {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ImageGroupService imageGroupService;

    @Autowired
    protected DeliveryInfoRepository deliveryInfoRepository;

    @Autowired
    protected DeliveryInfoService deliveryInfoService;

    protected ImageGroup setupImageGroup() {
        return imageGroupService.createImageGroup(Type.USER);
    }

    protected User setupUser(String email, String password, String nickname, ImageGroup imageGroup) {
        User user = TestEntityFactory.createUser(email, password, nickname, imageGroup);
        return user;
    }
}
