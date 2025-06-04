package goorm.athena.domain.user.controller;

import goorm.athena.domain.user.RefreshControllerIntegrationTestSupport;
import goorm.athena.domain.user.dto.response.RefreshTokenResponse;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RefreshControllerImplTest extends RefreshControllerIntegrationTestSupport{

    @DisplayName("액세스 토큰을 갱신할 때 리프레시 토큰이 유효하면 새로운 액세스 토큰을 발급한다.")
    @WithMockUser(username = "user", roles = {"USER"})
    @Test
    void requestRefresh_success() throws Exception {
        // given
        String oldAccessToken = "oldAccessToken";
        String refreshToken = "validRefreshToken";
        String newAccessToken = "newAccessToken";
        String newRefreshToken = "newRefreshToken";

        RefreshTokenResponse expectedResponse = new RefreshTokenResponse(1L, newAccessToken, newRefreshToken);

        // stub
        given(jwtTokenizer.extractBearerToken("Bearer " + oldAccessToken)).willReturn(oldAccessToken);
        given(refreshTokenService.reissueToken(eq(oldAccessToken), eq(refreshToken), any(HttpServletResponse.class)))
                .willReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/api/refreshToken/ReissueRefresh")
                        .cookie(new Cookie("refreshToken", refreshToken))
                        .header("Authorization", "Bearer " + oldAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(newAccessToken))
                .andExpect(jsonPath("$.refreshToken").value(newRefreshToken));
    }

    @DisplayName("액세스 토큰을 갱신할 때 리프레시 토큰이 만료되면 AUTH_TOKEN_EXPIRED 에러를 리턴한다.")
    @WithMockUser(username = "user", roles = {"USER"})
    @Test
    void requestRefresh_FAILED() throws Exception {
        // given
        String validAccessToken = "validAccessToken";
        String oldRefreshToken = "OldRefreshToken";

        // stub
        given(jwtTokenizer.extractBearerToken("Bearer " + validAccessToken)).willReturn(validAccessToken);
        given(refreshTokenService.reissueToken(eq(validAccessToken), eq(oldRefreshToken), any(HttpServletResponse.class)))
                .willThrow(new JwtException("토큰이 만료되었습니다."));

        // when // the
        mockMvc.perform(post("/api/refreshToken/ReissueRefresh")
                        .cookie(new Cookie("refreshToken", oldRefreshToken))
                        .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("토큰이 만료되었습니다."));
    }
}


