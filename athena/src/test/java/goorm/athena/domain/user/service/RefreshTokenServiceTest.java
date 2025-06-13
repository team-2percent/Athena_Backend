package goorm.athena.domain.user.service;

import goorm.athena.domain.user.RefreshTokenIntegrationTestSupport;
import goorm.athena.domain.user.dto.response.RefreshTokenResponse;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
class RefreshTokenServiceTest extends RefreshTokenIntegrationTestSupport {

    @DisplayName("토큰을 재발급할 때 리프레시 토큰이 Null이라면 에러를 리턴한다.")
    @Test
    void reissueToken_whenRefreshTokenIsNull_thenThrowException() {
        // given
        String accessToken = "someAccessToken";
        String refreshToken = null;

        // when & then
        assertThatThrownBy(() -> refreshTokenCommandService.reissueToken(accessToken, refreshToken, response))
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
                refreshTokenCommandService.reissueToken(accessToken, refreshToken, response)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REFRESHTOKEN_NOT_FOUND);
    }

    @DisplayName("토큰을 재발급할 때 액세스, 리프레시 토큰의 만료되지 않았다면 그대로 두 토큰을 리턴한다.")
    @Test
    void reissueToken_whenBothTokensValid_thenReturnSameTokens() {
        // given
        User user = userRepository.findById(12L).get();

        String accessToken = jwtTokenizer.createAccessToken(
                user.getId(), user.getNickname(), user.getRole().name(), 24 * 60 * 60 * 1000L);

        String refreshToken = jwtTokenizer.createRefreshToken(
                user.getId(), user.getNickname(), user.getRole().name(), 24 * 60 * 60 * 1000L);

        // when
        RefreshTokenResponse actualResponse = refreshTokenCommandService.reissueToken(accessToken, refreshToken, response);

        // then
        assertThat(actualResponse.accessToken()).isEqualTo(accessToken);
        assertThat(actualResponse.refreshToken()).isEqualTo(refreshToken);

    }

    @DisplayName("토큰을 재발급할 때 액세스 토큰이 만료되고 리프레시 토큰이 만료되지 않았다면 액세스 토큰을 재발급한다.")
    @Test
    void reissueToken_whenAccessExpiredRefreshValid_thenReturnNewAccessToken() {
        User user = userRepository.findById(12L).get();

        // given
        String accessToken = jwtTokenizer.createAccessToken(
                user.getId(), user.getNickname(), user.getRole().name(), 1L);

        String refreshToken = jwtTokenizer.createRefreshToken(
                user.getId(), user.getNickname(), user.getRole().name(), 24 * 60 * 60 * 1000L);
        // when
        RefreshTokenResponse actualResponse = refreshTokenCommandService.reissueToken(accessToken, refreshToken, response);

        // then
        assertThat(actualResponse.accessToken()).isNotEqualTo(accessToken);
        assertThat(actualResponse.refreshToken()).isEqualTo(refreshToken);
    }

    @DisplayName("토큰을 재발급할 때 액세스 토큰이 만료되지 않고 리프레시 토큰이 만료되었다면 에러를 리턴한다.")
    @Test
    void reissueToken_whenRefreshExpired_thenThrowRefreshTokenExpiredException() {
        User user = userRepository.findById(12L).get();

        // given
        String accessToken = jwtTokenizer.createAccessToken(
                user.getId(), user.getNickname(), user.getRole().name(), 24 * 60 * 60 * 1000L);

        String refreshToken = jwtTokenizer.createRefreshToken(
                user.getId(), user.getNickname(), user.getRole().name(), 1L);

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                refreshTokenCommandService.reissueToken(accessToken, refreshToken, response)
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
        User user = userRepository.findById(12L).get();

        String accessToken = jwtTokenizer.createAccessToken(
                user.getId(), user.getNickname(), user.getRole().name(), 1L);

        String refreshToken = jwtTokenizer.createRefreshToken(
                user.getId(), user.getNickname(), user.getRole().name(), 1L);


        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                refreshTokenCommandService.reissueToken(accessToken, refreshToken, response)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTH_TOKEN_EXPIRED);
        assertThat(response.getHeader("Set-Cookie")).contains("Max-Age=0");
    }
}