package goorm.athena.domain.deliveryinfo;

import goorm.athena.domain.deliveryinfo.controller.DeliveryInfoControllerImpl;
import goorm.athena.domain.deliveryinfo.repository.DeliveryInfoRepository;
import goorm.athena.domain.deliveryinfo.service.DeliveryInfoService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.service.ImageGroupCommandService;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.global.jwt.util.LoginUserRequest;
import goorm.athena.util.IntegrationControllerTestSupport;
import goorm.athena.util.TestEntityFactory;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class DeliveryControllerIntegrationTestSupport extends IntegrationControllerTestSupport {
    protected DeliveryInfoControllerImpl controller;
    protected LoginUserRequest loginUserRequest;

    @Autowired
    protected DeliveryInfoService deliveryInfoService;

    @Autowired
    protected DeliveryInfoRepository deliveryInfoRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ImageGroupCommandService imageGroupCommandService;

    @BeforeEach
    void setUp() {
        controller = new DeliveryInfoControllerImpl(deliveryInfoService);
        loginUserRequest = new LoginUserRequest("123", 1L, Role.ROLE_USER);
    }

    protected User setupUser(String email, String password, String nickname, ImageGroup imageGroup) {
        User user = TestEntityFactory.createUser(email, password, nickname, imageGroup, Role.ROLE_USER);
        return user;
    }
}
