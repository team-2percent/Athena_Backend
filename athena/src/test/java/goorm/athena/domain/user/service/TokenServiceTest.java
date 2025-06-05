package goorm.athena.domain.user.service;

import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.user.TokenIntegrationTestSupport;
import goorm.athena.domain.user.entity.User;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TokenServiceTest extends TokenIntegrationTestSupport {


    @DisplayName("리프레시 토큰을 성공적으로 발급 시 응답 헤더에 Set-Cookie의 값에 refreshToken이 설정된다.")
    @Test
    void issueToken_shouldReturnRefreshTokenAndSetCookieHeader() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("123", "123", "123", imageGroup);
        HttpServletResponse response = spy(MockHttpServletResponse.class);
        ArgumentCaptor<String> cookieCaptor = ArgumentCaptor.forClass(String.class);

        // when
        String actualRefreshToken = tokenService.issueToken(user, response);

        verify(response).addHeader(eq("Set-Cookie"), cookieCaptor.capture());
        String setCookieHeader = cookieCaptor.getValue();

        assertThat(setCookieHeader).contains("refreshToken=" + actualRefreshToken);
        assertThat(setCookieHeader).contains("HttpOnly");
        assertThat(setCookieHeader).contains("Secure");
        assertThat(setCookieHeader).contains("Max-Age=604800");
        assertThat(setCookieHeader).contains("SameSite=None");
        assertThat(setCookieHeader).contains("Path=/");
    }
}