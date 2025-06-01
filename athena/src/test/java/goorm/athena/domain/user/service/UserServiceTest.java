package goorm.athena.domain.user.service;

import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.user.dto.request.UserCreateRequest;
import goorm.athena.domain.user.dto.request.UserLoginRequest;
import goorm.athena.domain.user.dto.request.UserUpdatePasswordRequest;
import goorm.athena.domain.user.dto.request.UserUpdateRequest;
import goorm.athena.domain.user.dto.response.*;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import goorm.athena.domain.user.UserIntegrationTestSupport;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


class UserServiceTest extends UserIntegrationTestSupport {

    @DisplayName("사용자 정보와 프로필 이미지가 주어지면 사용자 정보를 업데이트한다")
    @Test
    void updateUser_withImage_thenUpdateUserAndUploadImage() {
        // given
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest("newNick", "소개글", "https://link.com");

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);

        ImageGroup imageGroup = new ImageGroup();

        User user = User.builder()
                .email("user@example.com")
                .password("encodedPwd")
                .nickname("oldNick")
                .imageGroup(imageGroup)
                .build();

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserUpdateResponse response = userService.updateUser(userId, request, file);

        // then
        assertThat(response).isNotNull();
        assertThat(response.nickname()).isEqualTo("newNick");

        verify(userRepository).findById(userId);
        verify(imageService).uploadImages(List.of(file), imageGroup);
        verify(userRepository).save(any(User.class));
    }

    @DisplayName("프로필 이미지 없이 사용자의 정보를 업데이트한다")
    @Test
    void updateUser_withoutImage_thenUpdateOnlyUser() {
        // given
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest("newNick", "소개글", "https://link.com");

        MultipartFile file = null;

        ImageGroup imageGroup = new ImageGroup();

        User user = User.builder()
                .email("user@example.com")
                .password("encodedPwd")
                .nickname("oldNick")
                .imageGroup(imageGroup)
                .build();

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserUpdateResponse response = userService.updateUser(userId, request, file);

        // then
        assertThat(response).isNotNull();
        assertThat(response.nickname()).isEqualTo("newNick");

        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @DisplayName("로그인 한 유저의 자신의 유저 정보를 조회한다.")
    @Test
    void getUserById_withValidUser_returnsResponse() {
        // given
        Long userId = 1L;
        ImageGroup imageGroup = new ImageGroup();
        User user = User.builder().
                nickname("nick").
                imageGroup(imageGroup).
                build();

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserGetResponse response = userService.getUserById(userId);

        // then
        assertThat(response.nickname()).isEqualTo("nick");
    }

    @DisplayName("로그인 한 유저의 헤더에 보여줄 정보들을 조회한다.")
    @Test
    void getHeaderById_returnsHeaderResponse() {
        // given
        Long userId = 1L;
        ImageGroup imageGroup = new ImageGroup();
        User user = User.builder().nickname("nick").imageGroup(imageGroup).build();
        setId(user, 1L);

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserHeaderGetResponse response = userService.getHeaderById(userId);

        // then
        assertThat(response.nickname()).isEqualTo("nick");
    }

    @DisplayName("해당 유저 id의 유저 정보를 성공적으로 삭제한다.")
    @Test
    void deleteUser_successfullyDeletesUser() {
        // given
        Long userId = 1L;

        // when
        userService.deleteUser(userId);

        // then
        verify(userRepository).deleteById(userId);
    }

    @DisplayName("로그인한 유저의 저장된 비밀번호와 입력된 비밀번호가 같다면 true를 리턴한다.")
    @Test
    void checkPassword_returnsTrueWhenMatches() {
        // given
        Long userId = 1L;
        String rawPw = "raw";
        String encPw = "encoded";

        User user = User.builder().password(encPw).build();
        setId(user, 1L);

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPw, encPw)).thenReturn(true);

        boolean result = userService.checkPassword(userId, rawPw);

        // then
        assertThat(result).isTrue();
    }

    @DisplayName("복수의 유저 정보들을 조회한다.")
    @Test
    void getUserIdAll_returnsAllUserIds() {
        // given
        User user1 = User.builder().email("1l").build();
        User user2 = User.builder().email("2l").build();

        setId(user1, 1L);
        setId(user2, 2L);

        List<User> users = List.of(user1, user2);

        // when
        when(userRepository.findAll()).thenReturn(users);

        List<Long> result = userService.getUserIdAll();

        // then
        assertThat(result).containsExactly(1l, 2l);
    }

    @DisplayName("로그인 한 유저의 저장된 비밀번호와 입력된 비밀번호가 같다면 새 토큰을 발급한다.")
    @Test
    void validateUserCredentials_returnsLoginResponse() {
        // given
        String email = "user@example.com";
        String rawPassword = "pw";
        UserLoginRequest request = new UserLoginRequest(email, rawPassword);
        User user = User.builder().
                email(email).
                password("encodedPw").
                nickname("nick").
                build();
        setId(user, 1L);

        // when
        when(userRepository.findByEmail(email)).thenReturn(user);
        when(passwordEncoder.matches(rawPassword, "encodedPw")).thenReturn(true);
        when(jwtTokenizer.createAccessToken(1L, "nick", "ROLE_USER")).thenReturn("access-token");
        when(tokenService.issueToken(eq(user), any())).thenReturn("refresh-token");

        UserLoginResponse response = userService.validateUserCredentials(request, httpServletResponse);

        // then
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
    }

    @DisplayName("로그인 한 유저의 저장된 비밀번호와 입력된 비밀번호가 같다면 성공적으로 새 비밀번호로 변경한다.")
    @Test
    void updatePassword_success() {
        // given
        Long userId = 1L;
        String oldPw = "oldPw";
        String newPw = "newPw";
        String encodedPw = "encodedNewPw";

        UserUpdatePasswordRequest req = new UserUpdatePasswordRequest(oldPw, newPw);
        User user = User.builder().password("oldEncoded").build();
        setId(user, 1L);

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPw, "oldEncoded")).thenReturn(true);
        when(passwordEncoder.encode(newPw)).thenReturn(encodedPw);

        userService.updatePassword(userId, req);

        // then
        assertThat(user.getPassword()).isEqualTo(encodedPw);
    }

    @DisplayName("로그인 한 유저의 요약 정보들을 성공적으로 리턴한다.")
    @Test
    void getUserSummary_returnsSummaryResponse() {
        // given
        Long userId = 1L;
        User user = User.builder().nickname("nick").build();
        setId(user, 1L);

        user.update("nick", "소개", "https://example.com");

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserSummaryResponse response = userService.getUserSummary(userId);

        // then
        assertThat(response.linkUrl()).isEqualTo("https://example.com");
    }

    @DisplayName("로그인한 유저의 이미지 그룹으로 연결된 이미지를 성공적으로 조회한다.")
    @Test
    void getUserById_withImageGroup_returnsImageUrl() {
        // given
        Long userId = 1L;
        Long imageGroupId = 100L;

        ImageGroup imageGroup = ImageGroup.builder()
                .type(Type.USER)
                .build();
        ReflectionTestUtils.setField(imageGroup, "id", imageGroupId);

        User user = User.builder()
                .imageGroup(imageGroup)
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("tester")
                .build();

        given(imageService.getImage(imageGroupId)).willReturn("http://image.url/sample.jpg");

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UserGetResponse response = userService.getUserById(userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.imageUrl()).isEqualTo("http://image.url/sample.jpg");
    }

    @DisplayName("로그인 한 유저의 헤더 정보를 조회할 때 이미지 그룹이 없다면 이미지를 null로 리턴한다.")
    @Test
    void getHeaderById_whenImageGroupIsNull() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .imageGroup(null) // imageGroup이 null인 유저
                .build();

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserHeaderGetResponse response = userService.getHeaderById(userId);

        // then
        assertThat(response.imageUrl()).isEmpty();
    }

    @DisplayName("로그인 한 유저의 헤더 정보를 조회할 때 이미지 그룹이 있다면 이미지를 리턴한다.")
    @Test
    void getHeaderById_whenImageGroupIsNotNull() {
        // given
        Long userId = 1L;
        ImageGroup imageGroup = ImageGroup.builder().build();
        ReflectionTestUtils.setField(imageGroup, "id", 123L); // id 강제 세팅

        User user = User.builder()
                .imageGroup(imageGroup)
                .build();


        given(imageService.getImage(123L)).willReturn("http://image.url/sample.jpg");

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserHeaderGetResponse response = userService.getHeaderById(userId);

        // then
        assertThat(response.imageUrl()).isEqualTo("http://image.url/sample.jpg");
    }

    @DisplayName("로그인 한 유저의 정보를 조회할 때 이미지 그룹이 없다면 이미지를 null로 조회한다.")
    @Test
    void getUserById_whenImageGroupIsNull() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .imageGroup(null)
                .build();

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserGetResponse response = userService.getUserById(userId);

        // then
        assertThat(response.imageUrl()).isNull();
    }

    @DisplayName("로그인 한 유저의 정보를 조회할 때 이미지 그룹이 있다면 이미지를 조회한다.")
    @Test
    void getUserById_whenImageGroupIdIsNotNull() {
        // given
        Long userId = 1L;
        ImageGroup imageGroup = ImageGroup.builder().build();
        ReflectionTestUtils.setField(imageGroup, "id", 123L); // id 강제 세팅

        User user = User.builder()
                .imageGroup(imageGroup)
                .build();

        given(imageService.getImage(123L)).willReturn("http://image.url/sample.jpg");

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserGetResponse response = userService.getUserById(userId);

        // then
        assertThat(response.imageUrl()).isEqualTo("http://image.url/sample.jpg");
    }

    @Test
    void createUser_whenEmailNotExist_shouldSaveUser() {
        // given
        UserCreateRequest request = new UserCreateRequest("test@example.com", "123123", "1123");
        ImageGroup imageGroup = new ImageGroup();

        // when
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        UserCreateResponse response = userService.createUser(request, imageGroup);

        // then
        assertEquals(request.email(), response.email());
        verify(userRepository).save(any(User.class));
    }

    // 실패

    @DisplayName("유저 요약 정보를 조회할 시 존재하지 않는 유저라면 에러를 리턴한다")
    @Test
    void getUserSummary_throwsException_whenUserNotFound() {
        // given
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUserSummary(userId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getErrorMessage());
    }

    @DisplayName("회원가입을 진행할 시 이미 존재한 이메일이라면 에러를 리턴한다.")
    @Test
    void createUser_throwsException_whenEmailAlreadyExists() {
        // given
        String email = "existing@example.com";
        UserCreateRequest request = new UserCreateRequest(email, "password", "nickname");
        ImageGroup imageGroup = new ImageGroup();

        // when
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // then
        assertThatThrownBy(() -> userService.createUser(request, imageGroup))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.ALREADY_EXIST_USER.getErrorMessage());
    }

    @DisplayName("유저의 정보를 수정할 시 수정할 유저와 id가 일치하지 않을 경우 에러를 리턴한다.")
    @Test
    void updateUser_throwsException_whenUserNotFound() {
        // given
        Long userId = 123L;
        UserUpdateRequest request = new UserUpdateRequest("newNick", "소개글", "https://link.com");
        MultipartFile file = null;

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> userService.updateUser(userId, request, file))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getErrorMessage());
    }

    @DisplayName("로그인 한 유저의 저장된 비밀번호와 입력받은 비밀번호가 같지 않다면 false를 리턴한다.")
    @Test
    void checkPassword_returnsFalse_whenPasswordDoesNotMatch() {
        // given
        Long userId = 1L;
        String rawPw = "wrongPw";
        String encPw = "encoded";

        User user = User.builder().password(encPw).build();
        setId(user, userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPw, encPw)).thenReturn(false);

        // when
        boolean result = userService.checkPassword(userId, rawPw);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("유저를 수정할 때 파일이 null 또는 비어있으면 이미지 업로드가 호출되지 않고 정상 처리된다")
    void updateUser_fileNullOrEmpty_success() {
        // given
        Long userId = 1L;
        User user = User.builder().build();
        setId(user, 1L);
        UserUpdateRequest request = new UserUpdateRequest("newNick", "newIntro", "newUrl");
        MultipartFile file = Mockito.mock(MultipartFile.class);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(file.isEmpty()).willReturn(true);  // 비어있음

        given(userRepository.save(any(User.class))).willReturn(user);

        // when
        UserUpdateResponse response = userService.updateUser(userId, request, file);

        // then
        verify(imageService, never()).uploadImages(anyList(), any());
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("이미지 업로드 중 예외가 발생하면 에러를 리턴한다")
    void updateUser_imageUploadException_throwsException() {
        // given
        Long userId = 1L;
        User user = User.builder().build();
        setId(user, 1L);
        UserUpdateRequest request = new UserUpdateRequest("newNick", "newIntro", "newUrl");

        MultipartFile file = Mockito.mock(MultipartFile.class);
        given(file.isEmpty()).willReturn(false);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        doThrow(new RuntimeException("upload failed")).when(imageService).uploadImages(anyList(), any());

        // when
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUser(userId, request, file);
        });

        // then
        assertThat(exception.getMessage()).isEqualTo("upload failed");
    }

    @DisplayName("비밀번호를 변경할 때 로그인 한 유저의 비밀번호와 저장된 비밀번호가 같지 않다면 에러를 리턴한다.")
    @Test
    void updatePassword_비밀번호_틀렸을_때_예외_발생() {
        // given
        Long userId = 1L;
        UserUpdatePasswordRequest request = new UserUpdatePasswordRequest("wrongOldPassword", "newPassword");

        User user = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        given(userService.checkPassword(userId, request.oldPassword())).willReturn(false);


        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            userService.updatePassword(userId, request);
        });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_USER_PASSWORD);
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