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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@Transactional
class UserServiceTest extends UserIntegrationTestSupport {

    @DisplayName("사용자 정보와 프로필 이미지가 주어지면 사용자 정보를 업데이트한다")
    @Test
    void updateUser_withImage_thenUpdateUserAndUploadImage() throws IOException {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("123", "123", "123", imageGroup);
        userRepository.save(user);

        UserUpdateRequest request = new UserUpdateRequest("newNick", "소개글", "https://link.com");

        // MultipartFile 생성 (임시 WebP 이미지)
        BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "webp", os); // WebP 포맷 지원 라이브러리 필요

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "test.webp",
                "image/webp",
                new ByteArrayInputStream(os.toByteArray())
        );

        UserUpdateResponse response = userCommandService.updateUser(user.getId(), request, multipartFile);

        // then
        assertThat(response).isNotNull();
        User updated = userQueryService.getUser(user.getId());
        assertThat(updated.getNickname()).isEqualTo("newNick");


        /*
        File savedFile = new File("src/test/resources/static/images/" +
        assertThat(savedFile.exists()).isTrue();

         */
    }

    @DisplayName("프로필 이미지 없이 사용자의 정보를 업데이트한다")
    @Test
    void updateUser_withoutImage_thenUpdateOnlyUser() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("123", "123", "123", imageGroup);
        userRepository.save(user);

        UserUpdateRequest request = new UserUpdateRequest("newNick", "소개글", "https://link.com");

        UserUpdateResponse response = userCommandService.updateUser(user.getId(), request, null);

        // then
        assertThat(response).isNotNull();
        User updated = userQueryService.getUser(user.getId());
        assertThat(updated.getNickname()).isEqualTo("newNick");
    }

    @DisplayName("로그인 한 유저의 자신의 유저 정보를 조회한다.")
    @Test
    void getUserById_withValidUser_returnsResponse() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("123", "123", "nick", imageGroup);
        userRepository.save(user);

        // when
        UserGetResponse response = userQueryService.getUserById(user.getId());

        // then
        assertThat(response.nickname()).isEqualTo("nick");
    }

    @DisplayName("로그인 한 유저의 헤더에 보여줄 정보들을 조회한다.")
    @Test
    void getHeaderById_returnsHeaderResponse() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("123", "123", "nick", imageGroup);
        userRepository.save(user);

        // when
        UserHeaderGetResponse response = userQueryService.getHeaderById(user.getId());
        String imageUrl = imageService.getImage(user.getImageGroup().getId());

        // then
        assertThat(response.nickname()).isEqualTo("nick");
        assertThat(response.imageUrl()).isEqualTo(imageUrl);
    }

    @DisplayName("해당 유저 id의 유저 정보를 성공적으로 삭제한다.")
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

    @DisplayName("로그인한 유저의 저장된 비밀번호와 입력된 비밀번호가 같다면 true를 리턴한다.")
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

    @DisplayName("복수의 유저 정보들을 조회한다.")
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

    @DisplayName("로그인 한 유저의 저장된 비밀번호와 입력된 비밀번호가 같다면 새 토큰을 발급한다.")
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
        User user = setupUser("124", passwordEncoder.encode("123"), "nick2", null);
        userRepository.save(user);
        String oldPw = "123";
        String newPw = "125";

        UserUpdatePasswordRequest req = new UserUpdatePasswordRequest(oldPw, newPw);

        // when
        userCommandService.updatePassword(user.getId(), req);

        // then
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(passwordEncoder.matches(newPw, updatedUser.getPassword())).isTrue();
    }

    @DisplayName("로그인 한 유저의 요약 정보들을 성공적으로 리턴한다.")
    @Test
    void getUserSummary_returnsSummaryResponse() {
        // given
        User user = setupUser("124", passwordEncoder.encode("123"), "nick2", null);
        userRepository.save(user);

        user.update("nick", "소개", "https://example.com");

        // when
        UserSummaryResponse response = userQueryService.getUserSummary(user.getId());

        // then
        assertThat(response.linkUrl()).isEqualTo("https://example.com");
    }

    @DisplayName("로그인한 유저의 이미지 그룹으로 연결된 이미지를 성공적으로 조회한다.")
    @Test
    void getUserById_withImageGroup_returnsImageUrl() throws IOException {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("124", passwordEncoder.encode("123"), "nick2", imageGroup);
        userRepository.save(user);

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

        Optional<Image> savedImage = imageRepository.findFirstImageByImageGroupId(imageGroup.getId());
        assertThat(savedImage).isPresent();
        assertThat(savedImage.get().getFileName()).matches("[a-f0-9\\-]{36}\\.webp");
        assertThat(savedImage.get().getOriginalUrl()).endsWith(".webp"); // 확장자 포함 여부 확인
    }

    @DisplayName("로그인 한 유저의 헤더 정보를 조회할 때 이미지 그룹이 없다면 이미지를 null로 리턴한다.")
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

    @DisplayName("로그인 한 유저의 헤더 정보를 조회할 때 이미지 그룹이 있다면 이미지를 리턴한다.")
    @Test
    void getHeaderById_whenImageGroupIsNotNull() throws IOException {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("124", passwordEncoder.encode("123"), "nick2", imageGroup);
        userRepository.save(user);

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

        Optional<Image> savedImage = imageRepository.findFirstImageByImageGroupId(imageGroup.getId());
        assertThat(savedImage).isPresent();
        assertThat(savedImage.get().getFileName()).matches("[a-f0-9\\-]{36}\\.webp");
        assertThat(savedImage.get().getOriginalUrl()).endsWith(".webp"); // 확장자 포함 여부 확인
    }

    @DisplayName("로그인 한 유저의 정보를 조회할 때 이미지 그룹이 없다면 이미지를 null로 조회한다.")
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

    @DisplayName("로그인 한 유저의 정보를 조회할 때 이미지 그룹이 있다면 이미지를 조회한다.")
    @Test
    void getUserById_whenImageGroupIdIsNotNull() throws IOException {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("124", passwordEncoder.encode("123"), "nick2", imageGroup);
        userRepository.save(user);

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

        Optional<Image> savedImage = imageRepository.findFirstImageByImageGroupId(imageGroup.getId());
        assertThat(savedImage).isPresent();
        assertThat(savedImage.get().getFileName()).matches("[a-f0-9\\-]{36}\\.webp");
        assertThat(savedImage.get().getOriginalUrl()).endsWith(".webp"); // 확장자 포함 여부 확인
    }

    @DisplayName("로그인 한 유저의 정보를 조회할 때 이미지 그룹의 Id가 null이라면 이미지를 null로 리턴한다.")
    @Test
    void getUserById_whenImageGroupExistsButIdIsNull() throws Exception {
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

    @DisplayName("유저 회원가입 시 기존에 저장된 이메일이 없다면 회원가입을 성공적으로 진행한다.")
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

    @DisplayName("유저 요약 정보를 조회할 시 존재하지 않는 유저라면 에러를 리턴한다")
    @Test
    void getUserSummary_throwsException_whenUserNotFound() {
        // given
        User user = setupUser("123", passwordEncoder.encode("123"), "nick", null);
        userRepository.save(user);

        // when & then
        assertThatThrownBy(() -> userQueryService.getUserSummary(99L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getErrorMessage());
    }

    @DisplayName("회원가입을 진행할 시 이미 존재한 이메일이라면 에러를 리턴한다.")
    @Test
    void createUser_throwsException_whenEmailAlreadyExists() {
        // given
        User user = setupUser("123", passwordEncoder.encode("123"), "nick", null);
        userRepository.save(user);

        ImageGroup imageGroup = new ImageGroup();

        // when
        String email2 = "existing@example.com";
        UserCreateRequest request = new UserCreateRequest("123", "password", "nickname");

        // then
        assertThatThrownBy(() -> userCommandService.createUser(request, imageGroup))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.ALREADY_EXIST_USER.getErrorMessage());
    }

    @DisplayName("유저의 정보를 수정할 시 수정할 유저와 id가 일치하지 않을 경우 에러를 리턴한다.")
    @Test
    void updateUser_throwsException_whenUserNotFound() {
        // given
        User user = setupUser("123", passwordEncoder.encode("123"), "nick", null);
        userRepository.save(user);
        UserUpdateRequest request = new UserUpdateRequest("newNick", "소개글", "https://link.com");
        MultipartFile file = null;

        // when & then
        assertThatThrownBy(() -> userCommandService.updateUser(2L, request, file))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getErrorMessage());
    }

    @DisplayName("로그인 한 유저의 저장된 비밀번호와 입력받은 비밀번호가 같지 않다면 false를 리턴한다.")
    @Test
    void checkPassword_returnsFalse_whenPasswordDoesNotMatch() {
        // given
        User user = setupUser("123", passwordEncoder.encode("123"), "nick", null);
        userRepository.save(user);
        String rawPw = "wrongPw";

        // when
        boolean result = userCommandService.checkPassword(user.getId(), rawPw);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("유저를 수정할 때 파일이 null 또는 비어있으면 이미지 업로드가 호출되지 않고 정상 처리된다")
    void updateUser_fileNullOrEmpty_success() {
        // given
        User user = setupUser("123", passwordEncoder.encode("123"), "nick", null);
        userRepository.save(user);

        UserUpdateRequest request = new UserUpdateRequest("newNick", "newIntro", "newUrl");

        // when
        UserUpdateResponse response = userCommandService.updateUser(user.getId(), request, null);

        // then
        assertThat(response).isNotNull();
        assertThat(response.nickname()).isEqualTo("newNick");
    }

    @Test
    @DisplayName("유저의 정보를 수정할 때 이미지 업로드 중 예외가 발생하면 에러를 리턴한다")
    void updateUser_imageUploadException_throwsException() throws IOException {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("124", passwordEncoder.encode("123"), "nick2", imageGroup);
        userRepository.save(user);

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
    @DisplayName("유저의 정보를 수정할 때 이미지 업로드 중 예외가 발생하면 에러를 리턴한다")
    void updateUser_imageUploadFileException_throwsException() throws IOException {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("124", passwordEncoder.encode("123"), "nick2", imageGroup);
        userRepository.save(user);

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
        User user = setupUser("123", passwordEncoder.encode("123"), "nick", null);
        userRepository.save(user);
        UserUpdatePasswordRequest request = new UserUpdatePasswordRequest("wrongOldPassword", "newPassword");

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            userCommandService.updatePassword(user.getId(), request);
        });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_USER_PASSWORD);
    }


}