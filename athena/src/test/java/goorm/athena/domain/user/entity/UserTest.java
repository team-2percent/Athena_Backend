package goorm.athena.domain.user.entity;

import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.service.ImageGroupCommandService;
import goorm.athena.domain.user.UserIntegrationTestSupport;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.util.IntegrationServiceTestSupport;
import goorm.athena.util.TestEntityFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UserTest extends UserIntegrationTestSupport {

    @Test
    void update() {
        User user = setupUser("123", "123", "123", null);
        user.update("1234", "12345", "12346");
        
        assertThat(user.getNickname()).isEqualTo("1234");
        assertThat(user.getSellerIntroduction()).isEqualTo("12345");
        assertThat(user.getLinkUrl()).isEqualTo("12346");
    }

    @Test
    void updatePassword() {
        User user = setupUser("123", "1234", "123", null);
        user.updatePassword("12345");
        
        assertThat(user.getPassword()).isEqualTo("12345");
    }

    @Test
    void createFullUser() {
        ImageGroup imageGroup = setupImageGroup();

        User user = User.createFullUser(imageGroup, "1212121212", "1212121213", "1212121214", Role.ROLE_USER, "123123123123", "123123123");

        assertThat(user.getEmail()).isEqualTo("1212121212");
        assertThat(user.getPassword()).isEqualTo("1212121213");
        assertThat(user.getNickname()).isEqualTo("1212121214");
        assertThat(user.getRole()).isEqualTo(Role.ROLE_USER);
        assertThat(user.getSellerIntroduction()).isEqualTo("123123123123");
        assertThat(user.getLinkUrl()).isEqualTo("123123123");
    }
}