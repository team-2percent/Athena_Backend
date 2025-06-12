package goorm.athena.domain.user.service;

import goorm.athena.domain.image.entity.Image;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.user.dto.request.UserCreateRequest;
import goorm.athena.domain.user.dto.request.UserLoginRequest;
import goorm.athena.domain.user.dto.request.UserUpdatePasswordRequest;
import goorm.athena.domain.user.dto.request.UserUpdateRequest;
import goorm.athena.domain.user.dto.response.*;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import goorm.athena.domain.user.UserIntegrationTestSupport;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@Transactional
class UserServiceTest extends UserIntegrationTestSupport {

    @DisplayName("유저가 정보를 수정할 때 사용자 정보와 프로필 이미지가 주어지면 사용자 정보를 수정하고 이미지를 업로드한다.")
    @Test
    void updateUser_withImage_thenUpdateUserAndUploadImage() throws IOException {
        // given
        User user = userRepository.findById(13L).get();

        UserUpdateRequest request = new UserUpdateRequest("newNick", "소개글", "https://link.com");

        // MultipartFile 생성 (임시 WebP 이미지)
        BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", os);

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "test.webp",
                "image/webp",
                new ByteArrayInputStream(os.toByteArray())
        );

        // when
        UserUpdateResponse response = userCommandService.updateUser(user.getId(), request, multipartFile);
        String imageUrl = imageQueryService.getImage(user.getImageGroup().getId());

        // then
        assertThat(response).isNotNull();
        User updated = userQueryService.getUser(user.getId());
        assertThat(updated.getNickname()).isEqualTo("newNick");
        assertThat(imageUrl).startsWith("http");
        assertThat(imageUrl).endsWith(".webp"); // 확장자 포함 여부 확인

    }

    @DisplayName("프로필 이미지 없이 사용자 정보를 수정하면 사용자 정보만 수정된다.")
    @Test
    void updateUser_withoutImage_thenUpdateOnlyUser() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = userRepository.findById(12L).get();

        UserUpdateRequest request = new UserUpdateRequest("newNick", "소개글", "https://link.com");

        UserUpdateResponse response = userCommandService.updateUser(user.getId(), request, null);
        String imageUrl = imageQueryService.getImage(user.getImageGroup().getId());

        // then
        assertThat(response).isNotNull();
        User updated = userQueryService.getUser(user.getId());
        assertThat(updated.getNickname()).isEqualTo("newNick");
        assertThat(imageUrl).isEqualTo("");
    }

    @Test
    @DisplayName("파일이 null이면 유저 정보 수정 시 이미지 업로드를 하지 않고 정보만 업데이트한다.")
    void updateUser_fileNull_success() {
        // given
        User user = userRepository.findById(15L).get();

        UserUpdateRequest request = new UserUpdateRequest("newNick", "newIntro", "newUrl");

        // when
        UserUpdateResponse response = userCommandService.updateUser(user.getId(), request, null);

        // then
        assertThat(response).isNotNull();
        assertThat(response.nickname()).isEqualTo("newNick");
    }

    @Test
    @DisplayName("유저를 수정할 때 파일이 null이면 비어있으면 이미지 업로드가 호출되지 않고 정상 처리된다")
    void updateUser_fileEmpty_success() throws IOException {
        // given
        User user = setupUser("123", passwordEncoder.encode("123"), "nick", null);
        userRepository.save(user);


        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "test.webp",
                "image/webp",
                InputStream.nullInputStream()
        );

        UserUpdateRequest request = new UserUpdateRequest("newNick", "newIntro", "newUrl");

        // when
        UserUpdateResponse response = userCommandService.updateUser(user.getId(), request, multipartFile);

        // then
        assertThat(response).isNotNull();
        assertThat(response.nickname()).isEqualTo("newNick");
    }

    @DisplayName("로그인한 유저가 자신의 정보를 조회하면 해당 유저 정보가 반환된다.")
    @Test
    void getUserById_withValidUser_returnsResponse() {
        // given
        User user = userRepository.findById(12L).get();

        // when
        UserGetResponse response = userQueryService.getUserById(user.getId());

        // then
        assertThat(response.nickname()).isEqualTo("User12");
        assertThat(response.email()).isEqualTo("user12@example.com");
    }

    @DisplayName("로그인한 유저가 헤더 정보를 조회하면 닉네임과 이미지 URL이 반환된다.")
    @Test
    void getHeaderById_returnsHeaderResponse() {
        // given
        User user = userRepository.findById(10L).get();
        String imageUrl = imageQueryService.getImage(user.getImageGroup().getId());

        // when
        UserHeaderGetResponse response = userQueryService.getHeaderById(user.getId());

        // then
        assertThat(response.nickname()).isEqualTo("User10");
        assertThat(response.imageUrl()).isEqualTo(imageUrl);
    }

    @DisplayName("유저 ID로 삭제 요청 시 해당 유저 정보가 성공적으로 삭제된다.")
    @Test
    void deleteUser_successfullyDeletesUser() {
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("123", passwordEncoder.encode("123"), "nick", imageGroup);
        userRepository.save(user);

        // when
        userCommandService.deleteUser(user.getId());

        // then
        boolean exists = userRepository.findById(user.getId()).isPresent();
        assertThat(exists).isFalse(); // 삭제되었는지 확인
    }

    @DisplayName("로그인한 유저의 입력 비밀번호가 저장된 비밀번호와 일치하면 true를 반환한다.")
    @Test
    void checkPassword_returnsTrueWhenMatches() {
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("123", passwordEncoder.encode("123"), "nick", imageGroup);
        userRepository.save(user);

        // given
        String encPw = "123";

        // when
        boolean result = userCommandService.checkPassword(user.getId(), encPw);

        // then
        assertThat(result).isTrue();
    }

    @DisplayName("전체 유저 ID 목록을 조회하면 저장된 모든 유저 ID를 반환한다.")
    @Test
    void getUserIdAll_returnsAllUserIds() {

        ImageGroup imageGroup = setupImageGroup();
        User user1 = setupUser("123", passwordEncoder.encode("123"), "nick", imageGroup);
        User user2 = setupUser("124", passwordEncoder.encode("123"), "nick2", null);
        int expectedSize = userRepository.findAll().size();
        userRepository.saveAll(List.of(user1, user2));

        // when
        List<Long> result = userQueryService.getUserIdAll();

        // then
        assertThat(result).hasSize(expectedSize + 2);
    }

    @DisplayName("로그인한 유저의 입력 비밀번호가 저장된 비밀번호와 일치하면 새 토큰을 발급한다.")
    @Test
    void validateUserCredentials_returnsLoginResponse() {
        // given
        UserLoginRequest request = new UserLoginRequest("124", "123");
        User user = setupUser("124", passwordEncoder.encode("123"), "nick2", null);
        userRepository.save(user);

        // when
        UserLoginResponse response = userCommandService.validateUserCredentials(request, httpServletResponse);
        String accessToken = jwtTokenizer.createAccessToken(user.getId(), user.getNickname(), user.getRole().name());
        String refreshToken = jwtTokenizer.createRefreshToken(user.getId(), user.getNickname(), user.getRole().name());

        // then
        assertThat(response.accessToken()).isNotNull();
        assertThat(response.refreshToken()).isNotNull();
        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.refreshToken()).isEqualTo(refreshToken);
    }

    @DisplayName("로그인 한 유저의 저장된 비밀번호와 입력된 비밀번호가 같다면 성공적으로 새 비밀번호로 변경한다.")
    @Test
    void updatePassword_success() {
        // given
        User user = userRepository.findById(31L).get();
        String oldPw = user.getPassword();
        String newPw = "125";


        UserUpdatePasswordRequest req = new UserUpdatePasswordRequest("123", newPw);

        // when
        userCommandService.updatePassword(user.getId(), req);

        // then
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(passwordEncoder.matches(newPw, updatedUser.getPassword())).isTrue();
    }

    @DisplayName("로그인 한 유저의 요약 정보를 조회하면 요약 응답을 성공적으로 반환한다.")
    @Test
    void getUserSummary_returnsSummaryResponse() {
        // given
        User user = userRepository.findById(1L).get();

        user.update("nick", "소개", "https://example.com");

        // when
        UserSummaryResponse response = userQueryService.getUserSummary(user.getId());

        // then
        assertThat(response.sellerIntroduction()).isEqualTo("소개");
        assertThat(response.linkUrl()).isEqualTo("https://example.com");
    }

    @DisplayName("로그인한 유저가 이미지 그룹에 연결된 이미지를 조회하면 이미지 URL을 성공적으로 반환한다.")
    @Test
    void getUserById_withImageGroup_returnsImageUrl() throws IOException {
        // given
        User user = userRepository.findById(4L).get();

        // 실제 이미지 파일 생성
        BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", os);

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "test.webp", "image/webp", new ByteArrayInputStream(os.toByteArray())
        );

        // when
        UserUpdateRequest request = new UserUpdateRequest("123", "123" , "123");
        userCommandService.updateUser(user.getId(), request, multipartFile);
        UserGetResponse response = userQueryService.getUserById(user.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.imageUrl()).isNotBlank();
        assertThat(response.imageUrl()).startsWith("http"); // 도메인 붙었는지 확인

        Optional<Image> savedImage = imageRepository.findFirstImageByImageGroupId(user.getImageGroup().getId());
        assertThat(savedImage).isPresent();
        assertThat(savedImage.get().getFileName()).matches("[a-f0-9\\-]{36}\\.webp");
        assertThat(savedImage.get().getOriginalUrl()).endsWith(".webp"); // 확장자 포함 여부 확인
    }

    @DisplayName("로그인한 유저가 헤더 정보를 조회할 때 이미지 그룹이 없으면 이미지 URL은 null로 반환된다.")
    @Test
    void getHeaderById_whenImageGroupIsNull() {
        // given
        User user = setupUser("124", passwordEncoder.encode("123"), "nick2", null);
        userRepository.save(user);

        // when
        UserHeaderGetResponse response = userQueryService.getHeaderById(user.getId());

        // then
        assertThat(response.imageUrl()).isEmpty();
    }

    @DisplayName("로그인한 유저가 헤더 정보를 조회할 때 이미지 그룹이 있으면 이미지 URL을 반환한다.")
    @Test
    void getHeaderById_whenImageGroupIsNotNull() {
        // given
        User user = userRepository.findById(10L).get();

        UserGetResponse response = userQueryService.getUserById(user.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.imageUrl()).isNotBlank();
        assertThat(response.imageUrl()).startsWith("http"); // 도메인 붙었는지 확인

        Optional<Image> savedImage = imageRepository.findFirstImageByImageGroupId(user.getImageGroup().getId());
        assertThat(savedImage).isPresent();
        assertThat(savedImage.get().getFileName()).matches("test/webp");
        assertThat(savedImage.get().getOriginalUrl()).endsWith(".webp"); // 확장자 포함 여부 확인
    }

    @DisplayName("로그인한 유저가 정보를 조회할 때 이미지 그룹이 없으면 이미지 URL은 null로 반환된다.")
    @Test
    void getUserById_whenImageGroupIsNull() {
        // given
        User user = setupUser("123", passwordEncoder.encode("123"), "nick", null);
        userRepository.save(user);

        // when
        UserGetResponse response = userQueryService.getUserById(user.getId());

        // then
        assertThat(response.imageUrl()).isNull();
    }

    @DisplayName("로그인한 유저가 정보를 조회할 때 이미지 그룹이 있으면 이미지 URL을 반환한다.")
    @Test
    void getUserById_whenImageGroupIdIsNotNull() {
        // given
        User user = userRepository.findById(10L).get();

        // when
        UserGetResponse response = userQueryService.getUserById(user.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.imageUrl()).isNotBlank();
        assertThat(response.imageUrl()).startsWith("http"); // 도메인 붙었는지 확인

        Optional<Image> savedImage = imageRepository.findFirstImageByImageGroupId(user.getImageGroup().getId());
        assertThat(savedImage).isPresent();
        assertThat(savedImage.get().getOriginalUrl()).endsWith(".webp"); // 확장자 포함 여부 확인
    }

    @DisplayName("로그인한 유저가 정보를 조회할 때 이미지 그룹은 있지만 ID가 null이면 이미지 URL은 null로 반환된다.")
    @Test
    void getUserById_whenImageGroupExistsButIdIsNull(){
        // given
        ImageGroup imageGroup = new ImageGroup();
        setField(imageGroup, "id", null); // 리플렉션으로 ID를 null로 설정

        User user = setupUser("test@email.com", passwordEncoder.encode("123"), "nickname", imageGroup);
        userRepository.save(user);

        // when
        UserGetResponse response = userQueryService.getUserById(user.getId());

        // then
        assertThat(response.imageUrl()).isNull(); // 혹은 비어있음
    }

    @DisplayName("유저가 회원가입할 때 기존에 저장된 이메일이 없으면 회원가입에 성공한다.")
    @Test
    void createUser_whenEmailNotExist_shouldSaveUser() {
        // given
        UserCreateRequest request = new UserCreateRequest("test@example.com", "123123", "1123");
        ImageGroup imageGroup = new ImageGroup();

        // when
        UserCreateResponse response = userCommandService.createUser(request, imageGroup);

        // then
        assertEquals(request.email(), response.email());
    }

    // 실패

    @DisplayName("유저가 존재하지 않으면 요약 정보를 조회할 때 에러를 리턴한다.")
    @Test
    void getUserSummary_throwsException_whenUserNotFound() {
        // given

        // when & then
        assertThatThrownBy(() -> userQueryService.getUserSummary(2000000L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getErrorMessage());
    }

    @DisplayName("이메일이 이미 존재하면 회원가입 시 에러를 리턴한다.")
    @Test
    void createUser_throwsException_whenEmailAlreadyExists() {
        // given

        // when
        UserCreateRequest request = new UserCreateRequest("user1@example.com", "password", "nickname");

        // then
        assertThatThrownBy(() -> userCommandService.createUser(request, null))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.ALREADY_EXIST_USER.getErrorMessage());
    }

    @DisplayName("유저 ID가 일치하지 않으면 정보를 수정할 때 에러를 리턴한다.")
    @Test
    void updateUser_throwsException_whenUserNotFound() {
        // given
        User user = userRepository.findById(11L).get();
        UserUpdateRequest request = new UserUpdateRequest("newNick", "소개글", "https://link.com");
        MultipartFile file = null;

        // when & then
        assertThatThrownBy(() -> userCommandService.updateUser(200000L, request, file))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getErrorMessage());
    }

    @DisplayName("로그인한 유저 비밀번호가 일치하지 않으면 비밀번호 확인 시 false를 리턴한다.")
    @Test
    void checkPassword_returnsFalse_whenPasswordDoesNotMatch() {
        // given
        User user = userRepository.findById(2L).get();
        String rawPw = "wrongPw";

        // when
        boolean result = userCommandService.checkPassword(user.getId(), rawPw);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("이미지 업로드 중 예외가 발생하면 유저 정보 수정 시 에러를 리턴한다")
    void updateUser_imageUploadException_throwsException() {
        // given
        User user = userRepository.findById(12L).get();

        // 실제 이미지 파일 생성
        UserUpdateRequest request = new UserUpdateRequest("123", "123", "123");

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "test.webp", "image/webp", "!".getBytes()
        );

        // when
        RuntimeException exception = assertThrows(CustomException.class, () -> {
            userCommandService.updateUser(user.getId(), request, multipartFile);
        });

        // then
        assertThat(exception.getMessage()).isEqualTo("원본 이미지 업로드에 실패했습니다.");
    }

    @Test
    @DisplayName("이미지 확장자가 올바르지 않으면 유저 정보 수정 시 에러를 리턴한다.")
    void updateUser_imageUploadFileException_throwsException() throws IOException {
        // given
        User user = userRepository.findById(12L).get();

        // 실제 이미지 파일 생성
        BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "123", os);

        UserUpdateRequest request = new UserUpdateRequest("123", "123", "123");

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "test.exe", "image/exe", "fake image".getBytes()
        );

        // when
        RuntimeException exception = assertThrows(CustomException.class, () -> {
            userCommandService.updateUser(user.getId(), request, multipartFile);
        });

        // then
        assertThat(exception.getMessage()).isEqualTo("이미지 확장자가 올바르지 않습니다.");
    }

    @DisplayName("비밀번호를 변경할 때 로그인 한 유저의 비밀번호와 저장된 비밀번호가 같지 않다면 에러를 리턴한다.")
    @Test
    void updatePassword_비밀번호_틀렸을_때_예외_발생() {
        // given
        User user = userRepository.findById(30L).get();
        UserUpdatePasswordRequest request = new UserUpdatePasswordRequest("wrongOldPassword", "newPassword");

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            userCommandService.updatePassword(user.getId(), request);
        });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_USER_PASSWORD);
    }
}