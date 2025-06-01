package goorm.athena.domain.user.service;

import goorm.athena.domain.user.TokenIntegrationTestSupport;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;

import static org.mockito.Mockito.eq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TokenServiceTest extends TokenIntegrationTestSupport {

    @DisplayName("리프레시 토큰을 성공적으로 발급 시 응답 헤더에 Set-Cookie의 값에 refreshToken이 설정된다.")
    @Test
    void issueToken_shouldReturnRefreshTokenAndSetCookieHeader() {
        // given
        User user = User.builder()
                .nickname("nickname")
                .build();
        setId(user, 1L);

        String expectedRefreshToken = "mocked-refresh-token";

        when(jwtTokenizer.createRefreshToken(user.getId(), user.getNickname(), user.getRole().name()))
                .thenReturn(expectedRefreshToken);

        ArgumentCaptor<String> cookieCaptor = ArgumentCaptor.forClass(String.class);

        // when
        String actualRefreshToken = tokenService.issueToken(user, response);

        // then
        assertThat(actualRefreshToken).isEqualTo(expectedRefreshToken);

        // Set-Cookie 헤더가 제대로 추가되었는지 검증
        verify(response).addHeader(eq("Set-Cookie"), cookieCaptor.capture());
        String setCookieHeader = cookieCaptor.getValue();

        assertThat(setCookieHeader).contains("refreshToken=" + expectedRefreshToken);
        assertThat(setCookieHeader).contains("HttpOnly");
        assertThat(setCookieHeader).contains("Secure");
        assertThat(setCookieHeader).contains("Max-Age=604800");
        assertThat(setCookieHeader).contains("SameSite=None");
        assertThat(setCookieHeader).contains("Path=/");
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