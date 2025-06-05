package goorm.athena.domain.user.controller;

import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.user.UserControllerIntegrationTestSupport;
import goorm.athena.domain.user.dto.request.UserCreateRequest;
import goorm.athena.domain.user.dto.request.UserLoginRequest;
import goorm.athena.domain.user.dto.request.UserUpdateRequest;
import goorm.athena.domain.user.dto.response.*;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import goorm.athena.global.jwt.util.LoginUserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerImplTest extends UserControllerIntegrationTestSupport{
    @DisplayName("로그인 한 사용자가 로그아웃을 성공했다면 쿠키에 저장된 리프레시 토큰을 삭제한다.")
    @Test
    void logout_success() {
        // given
        String refreshToken = "valid-refresh-token";

        // when
        ResponseEntity<Void> response = controller.logout(loginUserRequest, refreshToken, httpServletResponse);

        // then
        assertEquals(200, response.getStatusCodeValue());
        verify(fcmTokenService, times(1)).deleteToken(1L);
        verify(refreshTokenService, times(1)).deleteRefreshToken(httpServletResponse);
    }

    @DisplayName("로그인 한 사용자가 리프레시 토큰이 null일 때 로그아웃을 하면  에러를 리턴한다.")
    @Test
    void logout_throwsException_whenRefreshTokenIsNull() {
        // given
        String refreshToken = null;

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            controller.logout(loginUserRequest, refreshToken, httpServletResponse);
        });

        assertEquals(ErrorCode.REFRESHTOKEN_NOT_FOUND, exception.getErrorCode());
        verify(fcmTokenService, never()).deleteToken(anyLong());
        verify(refreshTokenService, never()).deleteRefreshToken(any());
    }

    @DisplayName("로그인 한 사용자가 refreshToken이 비어있을 때 로그아웃을 하면 에러를 리턴한다.")
    @Test
    void logout_throwsException_whenRefreshTokenIsEmpty() {
        // given
        String refreshToken = "";

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            controller.logout(loginUserRequest, refreshToken, httpServletResponse);
        });

        assertEquals(ErrorCode.REFRESHTOKEN_NOT_FOUND, exception.getErrorCode());
        verify(fcmTokenService, never()).deleteToken(anyLong());
        verify(refreshTokenService, never()).deleteRefreshToken(any());
    }

    @DisplayName("입력받은 정보로 사용자가 회원가입을 성공적으로 진행한다.")
    @Test
    void createUser() {
        // given
        UserCreateRequest request = new UserCreateRequest("123", "123", "123");
        ImageGroup mockImageGroup = ImageGroup.builder().type(Type.USER).build();
        setId(mockImageGroup, 1L);

        UserCreateResponse mockResponse = new UserCreateResponse(1L, "test@example.com", "nickname", Role.ROLE_USER);

        when(imageGroupService.createImageGroup(Type.USER)).thenReturn(mockImageGroup);
        when(userService.createUser(request, mockImageGroup)).thenReturn(mockResponse);

        // when
        ResponseEntity<UserCreateResponse> response = controller.createUser(request);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals("test@example.com", response.getBody().email());
        assertEquals("nickname", response.getBody().nickname());

        verify(imageGroupService, times(1)).createImageGroup(Type.USER);
        verify(userService, times(1)).createUser(request, mockImageGroup);
    }


    @DisplayName("사용자가 로그인 정보와 DB에 저장된 정보와 같다면 성공적으로 로그인한다.")
    @Test
    void login_success() {
        // given
        UserLoginRequest loginRequest = new UserLoginRequest("test@example.com", "password123");
        UserLoginResponse expectedResponse = new UserLoginResponse("access-token", "refresh-token", 1L);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.validateUserCredentials(loginRequest, httpServletResponse)).thenReturn(expectedResponse);

        // when
        ResponseEntity<UserLoginResponse> response = controller.login(loginRequest, bindingResult, httpServletResponse);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().userId());
        assertEquals("access-token", response.getBody().accessToken());
        assertEquals("refresh-token", response.getBody().refreshToken());

        verify(userService, times(1)).validateUserCredentials(loginRequest, httpServletResponse);
    }

    @DisplayName("이용자가 로그인 형식에 이메일을 잘못 입력했다면 유효값 에러를 리턴한다. ")
    @Test
    void login_email_validationError() {
        // given
        UserLoginRequest loginRequest = new UserLoginRequest("bad-email", "123");
        when(bindingResult.hasErrors()).thenReturn(true);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            controller.login(loginRequest, bindingResult, httpServletResponse);
        });

        assertEquals(ErrorCode.VALIDATION_ERROR, exception.getErrorCode());
        verify(userService, never()).validateUserCredentials(any(), any());
    }

    @DisplayName("로그인 한 유저가 의도에 맞게 수정 정보에 맞게 입력했다면 성공적으로 유저 수정을 진행한다.")
    @Test
    void updateUser_success() {
        LoginUserRequest loginUserRequest = new LoginUserRequest("123", 1L, Role.ROLE_USER);
        UserUpdateRequest request = new UserUpdateRequest("123123", "123123", "123213");
        MultipartFile file = new MockMultipartFile("file", "profile.png", "image/png", new byte[0]);
        UserUpdateResponse expected = new UserUpdateResponse(1L, "newNickname", "newBio", "imageUrl");

        when(userService.updateUser(1L, request, file)).thenReturn(expected);

        ResponseEntity<UserUpdateResponse> response = controller.updateUser(loginUserRequest, request, file);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expected, response.getBody());
    }

    @DisplayName("다른 유저의 정보를 user의 ID를 기준으로 조회한다.")
    @Test
    void getUserById_success() {
        Long userId = 1L;
        UserGetResponse expected = new UserGetResponse(1L, "bio", "imageUrl", "123", "123", "123");

        when(userService.getUserById(userId)).thenReturn(expected);

        ResponseEntity<UserGetResponse> response = controller.getUserById(userId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expected, response.getBody());
    }

    @DisplayName("해당 유저 id의 사용자의 회원 탈퇴를 성공적으로 진행한다.")
    @Test
    void deleteUser_success() {
        Long userId = 1L;

        doNothing().when(userService).deleteUser(userId);

        ResponseEntity<Void> response = controller.deleteUser(userId);

        assertEquals(204, response.getStatusCodeValue());
        verify(userService).deleteUser(userId);
    }

    @DisplayName("로그인 한 유저의 헤더에 나타낼 정보들을 성공적으로 조회한다.")
    @Test
    void getHeader_success() {
        //given
        UserHeaderGetResponse expected = new UserHeaderGetResponse(1L, "imageUrl", "123");

        //when
        when(userService.getHeaderById(1L)).thenReturn(expected);
        ResponseEntity<UserHeaderGetResponse> response = controller.getHeader(loginUserRequest);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expected, response.getBody());
    }

    @DisplayName("로그인 한 유저의 내 프로필 정보들을 성공적으로 조회한다.")
    @Test
    void getUserProfile_success() {
        Long userId = 1L;
        UserGetResponse expected = new UserGetResponse(1L, "123", "123", "123", "123", "123");

        when(userService.getUserById(userId)).thenReturn(expected);

        ResponseEntity<UserGetResponse> response = controller.getUserProfile(userId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expected, response.getBody());
    }

    public static void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}