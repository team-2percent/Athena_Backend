package goorm.athena.domain.user.service;

import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.user.RefreshTokenIntegrationTestSupport;
import goorm.athena.domain.user.dto.response.RefreshTokenResponse;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenServiceTest extends RefreshTokenIntegrationTestSupport {

    @DisplayName("토큰을 재발급할 때 리프레시 토큰이 Null이라면 에러를 리턴한다.")
    @Test
    void reissueToken_whenRefreshTokenIsNull_thenThrowException() {
        // given
        String accessToken = "someAccessToken";
        String refreshToken = null;

        // when & then
        assertThatThrownBy(() -> refreshTokenService.reissueToken(accessToken, refreshToken, response))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.REFRESHTOKEN_NOT_FOUND.getErrorMessage());
    }

    @Test
    @DisplayName("토큰을 재발급할 때 리프레시 토큰이 비어있다면 에러를 리턴한다.")
    void reissueToken_whenRefreshTokenIsEmpty_thenThrowRefreshTokenNotFoundException() {
        // given
        String accessToken = "validAccessToken";
        String refreshToken = "";

        // when & then
        CustomException exception = assertThrows(CustomException.class, () ->
                refreshTokenService.reissueToken(accessToken, refreshToken, response)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REFRESHTOKEN_NOT_FOUND);
    }

    @DisplayName("토큰을 재발급할 때 액세스, 리프레시 토큰의 만료되지 않았다면 그대로 두 토큰을 리턴한다.")
    @Test
    void reissueToken_whenBothTokensValid_thenReturnSameTokens() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("test@email.com", "password123", "nickname", imageGroup);
        userRepository.save(user);

        String accessToken = jwtTokenizer.createAccessToken(
                user.getId(), user.getNickname(), user.getRole().name(), 24 * 60 * 60 * 1000L);

        String refreshToken = jwtTokenizer.createRefreshToken(
                user.getId(), user.getNickname(), user.getRole().name(), 24 * 60 * 60 * 1000L);

        // when
        RefreshTokenResponse actualResponse = refreshTokenService.reissueToken(accessToken, refreshToken, response);

        // then
        assertThat(actualResponse.accessToken()).isEqualTo(accessToken);
        assertThat(actualResponse.refreshToken()).isEqualTo(refreshToken);

    }

    @DisplayName("토큰을 재발급할 때 액세스 토큰이 만료되고 리프레시 토큰이 만료되지 않았다면 액세스 토큰을 재발급한다.")
    @Test
    void reissueToken_whenAccessExpiredRefreshValid_thenReturnNewAccessToken() {
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("test@email.com", "password123", "nickname", imageGroup);
        userRepository.save(user);

        // given
        String accessToken = jwtTokenizer.createAccessToken(
                user.getId(), user.getNickname(), user.getRole().name(), 1L);

        String refreshToken = jwtTokenizer.createRefreshToken(
                user.getId(), user.getNickname(), user.getRole().name(), 24 * 60 * 60 * 1000L);
        // when
        RefreshTokenResponse actualResponse = refreshTokenService.reissueToken(accessToken, refreshToken, response);

        // then
        assertThat(actualResponse.accessToken()).isNotEqualTo(accessToken);
        assertThat(actualResponse.refreshToken()).isEqualTo(refreshToken);
    }

    @DisplayName("토큰을 재발급할 때 액세스 토큰이 만료되지 않고 리프레시 토큰이 만료되었다면 에러를 리턴한다.")
    @Test
    void reissueToken_whenRefreshExpired_thenThrowRefreshTokenExpiredException() {
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("test@email.com", "password123", "nickname", imageGroup);
        userRepository.save(user);

        // given
        String accessToken = jwtTokenizer.createAccessToken(
                user.getId(), user.getNickname(), user.getRole().name(), 24 * 60 * 60 * 1000L);

        String refreshToken = jwtTokenizer.createRefreshToken(
                user.getId(), user.getNickname(), user.getRole().name(), 1L);

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                refreshTokenService.reissueToken(accessToken, refreshToken, response)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REFRESHTOKEN_EXPIRED);

        String setCookie = response.getHeader("Set-Cookie");
        assertThat(setCookie).isNotNull(); // 쿠키 삭제 로직 수행 확인
        assertThat(setCookie).contains("Max-Age=0"); // 삭제 확인
    }

    @DisplayName("토큰을 재발급할 때 액세스 토큰이 만료되고 리프레시 토큰이 만료되었다면 에러를 리턴한다.")
    @Test
    void reissueToken_whenBothTokensExpired_thenThrowAuthTokenExpiredException() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("test@email.com", "password123", "nickname", imageGroup);
        userRepository.save(user);

        String accessToken = jwtTokenizer.createAccessToken(
                user.getId(), user.getNickname(), user.getRole().name(), 1L);

        String refreshToken = jwtTokenizer.createRefreshToken(
                user.getId(), user.getNickname(), user.getRole().name(), 1L);


        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                refreshTokenService.reissueToken(accessToken, refreshToken, response)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTH_TOKEN_EXPIRED);
        assertThat(response.getHeader("Set-Cookie")).contains("Max-Age=0");
    }

    /*
    @DisplayName("헤더 값이 'Bearer '로 시작하면 'Bearer '를 제거한 토큰을 리턴한다.")
    @Test
    void extractBearerToken_withBearerPrefix() {
        String tokenWithBearer = "Bearer abc.def.ghi";

        String extracted = jwtTokenizer.extractBearerToken(tokenWithBearer);

        assertThat(extracted).isEqualTo("abc.def.ghi");
    }

    @DisplayName("헤더 값이 'Bearer '로 시작하지 않으면 원래 값을 리턴한다.")
    @Test
    void extractBearerToken_withoutBearerPrefix() {
        String rawToken = "abc.def.ghi";

        String extracted = jwtTokenizer.extractBearerToken(rawToken);

        assertThat(extracted).isEqualTo(rawToken);
    }

    @DisplayName("헤더 값이 null이면 null을 리턴한다.")
    @Test
    void extractBearerToken_nullHeader() {
        String extracted = jwtTokenizer.extractBearerToken(null);

        assertThat(extracted).isNull();
    }


    @DisplayName("토큰을 재발급할 때 리프레시 토큰이 잘못됐다면 토큰 형식이 잘못됐다는 에러를 리턴한다.")
    @Test
    void reissueToken_whenBothTokensMalformed_thenThrowAuthTokenMalformedException() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("test@email.com", "password123", "nickname", imageGroup);
        userRepository.save(user);

        String accessToken = jwtTokenizer.createAccessToken(
                user.getId(), user.getNickname(), user.getRole().name(), 1L);

        String refreshToken = "12#";

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                refreshTokenService.reissueToken(accessToken, refreshToken, response)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTH_MALFORMED_TOKEN);
        assertThat(response.getHeader("Set-Cookie")).contains("Max-Age=0");
    }

    @DisplayName("토큰을 재발급할 때 리프레시 토큰이 잘못됐다면 토큰 형식이 잘못됐다는 에러를 리턴한다.")
    @Test
    void reissueToken_whenBothTokensSignature_thenThrowAuthTokenInvalidSignatureException() throws Exception {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("test@email.com", "password123", "nickname", imageGroup);
        userRepository.save(user);

        String accessToken = jwtTokenizer.createAccessToken(
                user.getId(), user.getNickname(), user.getRole().name(), 1L);

        String refreshToken = jwtTokenizer.createRefreshToken(
                user.getId(), user.getNickname(), user.getRole().name(), 1L);

        // 토큰 서명 부분 조작
        String[] parts = refreshToken.split("\\.");
        parts[2] = "tamperedsignature";
        String tamperedToken = String.join(".", parts);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () ->
                refreshTokenService.reissueToken(accessToken, tamperedToken, response)
        );

        // 예외 메시지나 코드 확인
        assertEquals(ErrorCode.AUTH_INVALID_SIGNATURE, exception.getErrorCode());
    }
    @DisplayName("빈 토큰이면 AUTH_EMPTY_TOKEN 예외를 던진다.")
    @Test
    void validate_emptyToken() {
        // given
        String emptyToken = "Bearer ";

        // when & then
        assertThatThrownBy(() -> jwtTokenizer.isValidRefreshToken(emptyToken))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.AUTH_EMPTY_TOKEN.getErrorMessage());
    }

    @DisplayName("지원되지 않는 토큰이라면 에러를 리턴한다.")
    @Test
    void validate_UNSUPPORTED_TOKEN_ERROR() {
        // alg: none → UnsupportedJwtException 유발
        String header = Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes());

        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(
                ("{\"sub\":\"1234567890\",\"name\":\"John Doe\",\"iat\":1516239022}").getBytes());

        String token = header + "." + payload + "."; // 서명 없음

        assertThatThrownBy(() -> jwtTokenizer.isValidRefreshToken("Bearer " + token))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.AUTH_UNSUPPORTED_TOKEN.getErrorMessage());
    }

    @DisplayName("예상하지 못한 예외가 발생하면 AUTH_FAILED 예외를 던진다.")
    @Test
    void validate_unexpectedException_thenThrowAuthFailed() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("test@email.com", "password123", "nickname", imageGroup);
        userRepository.save(user);

        // 정상적인 accessToken 생성 (이 시점에서는 secret key가 있음)
        String refreshToken = jwtTokenizer.createRefreshToken(
                user.getId(), user.getNickname(), user.getRole().name(), 1L);

        // 정상적으로 생성된 토큰이지만, secret key를 아예 엉뚱한 값으로 바꿔서 검증 실패 유도
        ReflectionTestUtils.setField(jwtTokenizer, "refreshSecret", "wrongSecretKey".getBytes(StandardCharsets.UTF_8));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () ->
                jwtTokenizer.isValidRefreshToken("Bearer " + refreshToken)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTH_INVALID_TOKEN);
    }

     */
}