package goorm.athena.global.jwt.util;

import goorm.athena.domain.user.entity.User;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import goorm.athena.global.jwt.JwtTokenizerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class JwtTokenizerTest extends JwtTokenizerTestSupport {
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
        User user = setupUser("test@email.com", "password123", "nickname", null);
        userRepository.save(user);

        String accessToken = jwtTokenizer.createAccessToken(
                user.getId(), user.getNickname(), user.getRole().name(), 1L);

        String refreshToken = "12#";
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                refreshTokenService.reissueToken(accessToken, refreshToken, response)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTH_MALFORMED_TOKEN);
    }

    @DisplayName("토큰을 재발급할 때 리프레시 토큰이 잘못됐다면 토큰 형식이 잘못됐다는 에러를 리턴한다.")
    @Test
    void reissueToken_whenBothTokensSignature_thenThrowAuthTokenInvalidSignatureException() throws Exception {
        // given
        User user = setupUser("test@email.com", "password123", "nickname", null);
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
}