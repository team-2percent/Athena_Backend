package goorm.athena.domain.user.controller;

import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.notification.dto.FcmLoginRequest;
import goorm.athena.domain.user.UserControllerIntegrationTestSupport;
import goorm.athena.domain.user.dto.request.UserCreateRequest;
import goorm.athena.domain.user.dto.request.UserLoginRequest;
import goorm.athena.domain.user.dto.request.UserUpdateRequest;
import goorm.athena.domain.user.dto.response.*;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import goorm.athena.global.jwt.util.LoginUserRequest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.validation.BeanPropertyBindingResult;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
class UserControllerImplTest extends UserControllerIntegrationTestSupport{
    @DisplayName("로그인 한 사용자가 로그아웃을 성공했다면 쿠키에 저장된 리프레시 토큰을 삭제한다.")
    @Test
    void logout_success() {
        // given
        User user = setupUser("123", "123", "123", null);
        userRepository.save(user);
        String refreshToken = jwtTokenizer.createRefreshToken(user.getId(), user.getNickname(), user.getRole().name());

        LoginUserRequest loginRequest = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);

        FcmLoginRequest fcmLoginRequest =  new FcmLoginRequest(user.getId() ,"!2321312");
        fcmTokenService.saveToken(fcmLoginRequest);

        // when
        ResponseEntity<Void> response = controller.logout(loginRequest, refreshToken, httpServletResponse);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    @DisplayName("로그인 한 사용자가 리프레시 토큰이 null일 때 로그아웃을 하면  에러를 리턴한다.")
    @Test
    void logout_throwsException_whenRefreshTokenIsNull() {
        // given
        User user = setupUser("123", "123", "123", null);
        userRepository.save(user);
        String refreshToken = null;

        LoginUserRequest loginRequest = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            controller.logout(loginRequest, refreshToken, httpServletResponse);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REFRESHTOKEN_NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("RefreshToken을 찾지 못했습니다.");
    }

    @DisplayName("로그인 한 사용자가 refreshToken이 비어있을 때 로그아웃을 하면 에러를 리턴한다.")
    @Test
    void logout_throwsException_whenRefreshTokenIsEmpty() {
        // given
        User user = setupUser("123", "123", "123", null);
        userRepository.save(user);
        String refreshToken = "";

        LoginUserRequest loginRequest = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            controller.logout(loginRequest, refreshToken, httpServletResponse);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REFRESHTOKEN_NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("RefreshToken을 찾지 못했습니다.");
    }

    @Transactional
    @DisplayName("입력받은 정보로 사용자가 회원가입을 성공적으로 진행한다.")
    @Test
    void createUser() {
        // given
        UserCreateRequest request = new UserCreateRequest("test@example.com", "123", "nickname");
        // when
        ResponseEntity<UserCreateResponse> response = controller.createUser(request);

        int oldSize = userRepository.findAll().size();
        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(userRepository.findAll().size(), oldSize);
        assertEquals("test@example.com", response.getBody().email());
        assertEquals("nickname", response.getBody().nickname());
    }

    @Transactional
    @DisplayName("사용자가 로그인 정보와 DB에 저장된 정보와 같다면 성공적으로 로그인한다.")
    @Test
    void login_success() {
        // given
        UserLoginRequest loginRequest = new UserLoginRequest("test@example.com", passwordEncoder.encode("password123"));
        User user = setupUser("test@example.com", "password123", "123", null);
        userRepository.save(user);

        // when
        bindingResult = new BeanPropertyBindingResult(loginRequest, "loginRequest");
        ResponseEntity<UserLoginResponse> response = controller.login(loginRequest, bindingResult, httpServletResponse);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertThat(response.getBody().userId()).isEqualTo(user.getId());

        assertThat(jwtTokenizer.isValidAccessToken(response.getBody().accessToken())).isTrue();
        assertThat(jwtTokenizer.isValidRefreshToken(response.getBody().refreshToken())).isTrue();
    }


    @DisplayName("이용자가 로그인 형식에 이메일을 잘못 입력했다면 유효값 에러를 리턴한다. ")
    @Test
    void login_email_validationError() {
        // given
        UserLoginRequest loginRequest = new UserLoginRequest("ba", passwordEncoder.encode("password123"));
        User user = setupUser("test@example.com", "password123", "121231231323", null);
        userRepository.save(user);

        bindingResult = new BeanPropertyBindingResult(loginRequest, "loginRequest");
        bindingResult.rejectValue("email", "invalid", "이메일 형식이 잘못되었습니다.");

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            controller.login(loginRequest, bindingResult, httpServletResponse);
        });

        assertEquals(ErrorCode.VALIDATION_ERROR, exception.getErrorCode());
    }

    @DisplayName("로그인 한 유저가 의도에 맞게 수정 정보에 맞게 입력했다면 성공적으로 유저 수정을 진행한다.")
    @Test
    void updateUser_success() throws IOException {
        ImageGroup userImageGroup = setupImageGroup();
        User user = setupUser("test@example.com", "password123", "123", userImageGroup);
        userRepository.save(user);

        LoginUserRequest loginRequest = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);

        UserUpdateRequest request = new UserUpdateRequest("newNickname", "newBio", "imageUrl");
        BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", os); // WebP 포맷 지원 라이브러리 필요

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                new ByteArrayInputStream(os.toByteArray())
        );

        UserUpdateResponse expected = new UserUpdateResponse(user.getId(), "newNickname", "newBio", "imageUrl");

        ResponseEntity<UserUpdateResponse> response = controller.updateUser(loginRequest, request, file);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expected, response.getBody());
    }

    @DisplayName("다른 유저의 정보를 user의 ID를 기준으로 조회한다.")
    @Test
    void getUserById_success() {
        ImageGroup userImageGroup = setupImageGroup();
        User user = setupUser("test@example.com", "password123", "123", userImageGroup);
        userRepository.save(user);

        ResponseEntity<UserGetResponse> response = controller.getUserById(user.getId());

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(user.getNickname(), response.getBody().nickname());
        assertThat(user.getEmail()).isEqualTo(response.getBody().email());
    }

    @DisplayName("해당 유저 id의 사용자의 회원 탈퇴를 성공적으로 진행한다.")
    @Test
    void deleteUser_success() {
        // given
        User user = setupUser("test@example.com", "password123", "123", null);
        userRepository.save(user);

        // when
        ResponseEntity<Void> response = controller.deleteUser(user.getId());

        // then
        assertEquals(204, response.getStatusCodeValue());
        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @DisplayName("로그인 한 유저의 헤더에 나타낼 정보들을 성공적으로 조회한다.")
    @Test
    void getHeader_success() throws IOException {
        // given
        ImageGroup userImageGroup = setupImageGroup();
        User user = setupUser("test@example.com", "password123", "123", userImageGroup);
        userRepository.save(user);

        LoginUserRequest loginRequest = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);

        UserUpdateRequest request = new UserUpdateRequest("newNickname", "newBio", "imageUrl");
        BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", os); // WebP 포맷 지원 라이브러리 필요

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                new ByteArrayInputStream(os.toByteArray())
        );

        userService.updateUser(user.getId(), request, file);

        //when
        ResponseEntity<UserHeaderGetResponse> response = controller.getHeader(loginRequest);
        String expectImage = imageService.getImage(user.getImageGroup().getId());

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertThat(expectImage).isEqualTo(response.getBody().imageUrl());
    }

    @DisplayName("로그인 한 유저의 내 프로필 정보들을 성공적으로 조회한다.")
    @Test
    void getUserProfile_success() throws IOException {
        // given
        ImageGroup userImageGroup = setupImageGroup();
        User user = setupUser("test@example.com", "password123", "newNickname", userImageGroup);
        userRepository.save(user);

        UserUpdateRequest request = new UserUpdateRequest("newNickname", "newBio", "imageUrl");
        BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", os); // WebP 포맷 지원 라이브러리 필요

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                new ByteArrayInputStream(os.toByteArray())
        );

        userService.updateUser(user.getId(), request, file);

        String expectImage = imageService.getImage(user.getImageGroup().getId());

        // when
        ResponseEntity<UserGetResponse> response = controller.getUserProfile(user.getId());

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertThat(user.getNickname()).isEqualTo(response.getBody().nickname());
        assertThat(expectImage).isEqualTo(response.getBody().imageUrl());
    }
}