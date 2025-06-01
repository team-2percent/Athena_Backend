package goorm.athena.domain.user.controller;

import goorm.athena.domain.user.RefreshControllerIntegrationTestSupport;
import goorm.athena.domain.user.dto.response.RefreshTokenResponse;
import goorm.athena.domain.user.service.RefreshTokenService;
import goorm.athena.global.jwt.util.JwtTokenizer;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RefreshControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class RefreshControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private JwtTokenizer jwtTokenizer;

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



// Mock을 사용한 방법 (성공)

/*
class RefreshControllerImplTest extends RefreshControllerIntegrationTestSupport {

    @Test
    void requestRefresh_success() {
        // given
        String bearerHeader = "Bearer oldAccessToken";
        String accessToken = "oldAccessToken";
        String refreshToken = "validRefreshToken";

        String newAccessToken = "newAccessToken";
        String newRefreshToken = "newRefreshToken";

        RefreshTokenResponse expectedResponse = new RefreshTokenResponse(1L, newAccessToken, newRefreshToken);

        // stub
        when(jwtTokenizer.extractBearerToken(bearerHeader)).thenReturn(accessToken);
        when(refreshTokenService.reissueToken(accessToken, refreshToken, httpServletResponse))
                .thenReturn(expectedResponse);

        // when
        ResponseEntity<RefreshTokenResponse> response = refreshController.requestRefresh(
                refreshToken, bearerHeader, httpServletResponse
        );

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedResponse, response.getBody());

        verify(jwtTokenizer).extractBearerToken(bearerHeader);
        verify(refreshTokenService).reissueToken(accessToken, refreshToken, httpServletResponse);
    }

}


 */