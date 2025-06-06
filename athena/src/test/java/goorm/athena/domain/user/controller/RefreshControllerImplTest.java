package goorm.athena.domain.user.controller;

import goorm.athena.domain.user.RefreshControllerIntegrationTestSupport;
import goorm.athena.domain.user.dto.response.RefreshTokenResponse;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RefreshControllerImplTest extends RefreshControllerIntegrationTestSupport{

    @DisplayName("액세스 토큰을 갱신할 때 리프레시 토큰이 유효하면 새로운 액세스 토큰을 발급한다.")
    @WithMockUser(username = "user", roles = {"USER"})
    @Test
    void requestRefresh_success() throws Exception {
        // given
        User user = setupUser("test@email.com", "password123", "nickname",  null);
        userRepository.save(user);

        String accessToken = jwtTokenizer.createAccessToken(user.getId(), user.getNickname(), user.getRole().name());
        String refreshToken = jwtTokenizer.createRefreshToken(user.getId(), user.getNickname(), user.getRole().name());

        String header = "Bearer " + accessToken;
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        RefreshTokenResponse tokenResponse = controller.requestRefresh(refreshToken, header, response).getBody();

        // then
        assertEquals(200, response.getStatus());
        assertThat(tokenResponse).isNotNull();
        assertThat(tokenResponse.accessToken()).isNotEmpty();
        assertThat(tokenResponse.refreshToken()).isNotEmpty();

        assertThat(jwtTokenizer.isValidAccessToken(tokenResponse.accessToken())).isTrue();
        assertThat(jwtTokenizer.isValidRefreshToken(tokenResponse.refreshToken())).isTrue();
    }

    @DisplayName("액세스 토큰을 갱신할 때 리프레시 토큰이 만료되면 AUTH_TOKEN_EXPIRED 에러를 리턴한다.")
    @WithMockUser(username = "user", roles = {"USER"})
    @Test
    void requestRefresh_FAILED() {
        // given
        User user = setupUser("test@email.com", "password123", "nickname",  null);
        userRepository.save(user);

        String accessToken = jwtTokenizer.createAccessToken(user.getId(), user.getNickname(), user.getRole().name());
        String refreshToken = jwtTokenizer.createRefreshToken(user.getId(), user.getNickname(), user.getRole().name(), 1L);

        String header = "Bearer " + accessToken;
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            controller.requestRefresh(refreshToken, header, response);
        });

        // then
        assertEquals(ErrorCode.REFRESHTOKEN_EXPIRED, exception.getErrorCode());
        assertThat(exception.getMessage()).isEqualTo("리프레시 토큰이 만료되었습니다");
    }
}


