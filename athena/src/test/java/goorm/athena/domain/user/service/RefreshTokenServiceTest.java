package goorm.athena.domain.user.service;

import goorm.athena.domain.user.RefreshTokenIntegrationTestSupport;
import goorm.athena.domain.user.dto.response.RefreshTokenResponse;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

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
        String accessToken = "validAccessToken";
        String refreshToken = "validRefreshToken";

        given(jwtTokenizer.isValidAccessToken(accessToken)).willReturn(true);
        given(jwtTokenizer.isValidRefreshToken(refreshToken)).willReturn(true);

        Claims claims = mock(Claims.class);
        given(jwtTokenizer.parseAccessToken(accessToken)).willReturn(claims);
        given(claims.getSubject()).willReturn("1");

        // when

        RefreshTokenResponse expectedResponse = new RefreshTokenResponse(1L, accessToken, refreshToken);
        RefreshTokenResponse actualResponse = refreshTokenService.reissueToken(accessToken, refreshToken, response);

        // then
        assertThat(expectedResponse.accessToken()).isEqualTo(accessToken);
        assertThat(actualResponse.refreshToken()).isEqualTo(refreshToken);
    }

    @DisplayName("토큰을 재발급할 때 액세스 토큰이 만료되고 리프레시 토큰이 만료되지 않았다면 액세스 토큰을 재발급한다.")
    @Test
    void reissueToken_whenAccessExpiredRefreshValid_thenReturnNewAccessToken() {
        // given
        String accessToken = "expiredAccessToken";
        String refreshToken = "validRefreshToken";

        given(jwtTokenizer.isValidAccessToken(accessToken)).willReturn(false);
        given(jwtTokenizer.isValidRefreshToken(refreshToken)).willReturn(true);

        Claims refreshClaims = mock(Claims.class);
        given(jwtTokenizer.parseAccessToken(accessToken)).willReturn(refreshClaims);
        given(jwtTokenizer.parseRefreshToken(refreshToken)).willReturn(refreshClaims);
        given(refreshClaims.getSubject()).willReturn("1");

        String newAccessToken = "validAccessToken";
        given(jwtTokenizer.parseAccessToken(newAccessToken)).willReturn(refreshClaims);
        given(jwtTokenizer.isValidAccessToken(newAccessToken)).willReturn(true);

        // when
        RefreshTokenResponse expectedResponse = new RefreshTokenResponse(1L, newAccessToken, refreshToken);
        RefreshTokenResponse actualResponse = refreshTokenService.reissueToken(newAccessToken, refreshToken, response);

        // then
        assertThat(expectedResponse.accessToken()).isEqualTo(newAccessToken);
        assertThat(actualResponse.refreshToken()).isEqualTo(refreshToken);
    }

    @DisplayName("토큰을 재발급할 때 액세스 토큰이 만료되지 않고 리프레시 토큰이 만료되었다면 에러를 리턴한다.")
    @Test
    void reissueToken_whenRefreshExpired_thenThrowRefreshTokenExpiredException() {
        // givem
        String accessToken = "validAccessToken";
        String expiredRefreshToken = "expiredRefreshToken";

        given(jwtTokenizer.isValidAccessToken(accessToken)).willReturn(true);
        given(jwtTokenizer.isValidRefreshToken(expiredRefreshToken)).willReturn(false);

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                refreshTokenService.reissueToken(accessToken, expiredRefreshToken, response)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REFRESHTOKEN_EXPIRED);
        String setCookie = response.getHeader("Set-Cookie");
        assertThat(setCookie).isNotNull(); // 실제로 헤더가 세팅되었는지 확인
        assertThat(setCookie).contains("Max-Age=0"); // 쿠키 삭제가 되었는지 확인
    }

    @DisplayName("토큰을 재발급할 때 액세스 토큰이 만료되고 리프레시 토큰이 만료되었다면 에러를 리턴한다.")
    @Test
    void reissueToken_whenBothTokensExpired_thenThrowAuthTokenExpiredException() {
        //given
        String expiredAccessToken = "expiredAccessToken";
        String expiredRefreshToken = "expiredRefreshToken";

        given(jwtTokenizer.isValidAccessToken(expiredAccessToken)).willReturn(false);
        given(jwtTokenizer.isValidRefreshToken(expiredRefreshToken)).willReturn(false);

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                refreshTokenService.reissueToken(expiredAccessToken, expiredRefreshToken, response)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTH_TOKEN_EXPIRED);
        assertThat(response.getHeader("Set-Cookie")).contains("Max-Age=0");
    }
    @Test
    @DisplayName("Access는 만료되고 Refresh는 유효하면 AccessToken을 재발급한다")
    void reissueToken_whenAccessExpiredAndRefreshValid_thenIssueNewAccessToken() {
        // given
        String expiredAccessToken = "expiredAccessToken";
        String validRefreshToken = "validRefreshToken";
        String newAccessToken = "newAccessToken";

        Long userId = 1L;
        String nickname = "tester";
        String role = "ROLE_USER";

        // Access 만료
        given(jwtTokenizer.isValidAccessToken(expiredAccessToken)).willReturn(false);
        // Refresh 유효
        given(jwtTokenizer.isValidRefreshToken(validRefreshToken)).willReturn(true);

        // RefreshToken에서 Claims 추출
        Claims claims = mock(Claims.class);
        given(jwtTokenizer.parseRefreshToken(validRefreshToken)).willReturn(claims);
        given(claims.getSubject()).willReturn(String.valueOf(userId));

        // 사용자 정보 조회
        User user = User.builder()
                .nickname(nickname)
                .build();
        setId(user, 1L);
        given(userService.getUser(userId)).willReturn(user);

        // 새로운 AccessToken 생성
        given(jwtTokenizer.createAccessToken(userId, nickname, role)).willReturn(newAccessToken);

        // when
        RefreshTokenResponse responseDto = refreshTokenService.reissueToken(newAccessToken, validRefreshToken, response);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.accessToken()).isEqualTo(newAccessToken);
        assertThat(responseDto.refreshToken()).isEqualTo(validRefreshToken);
        assertThat(responseDto.userId()).isEqualTo(userId);
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