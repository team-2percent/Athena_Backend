package goorm.athena.domain.user.service;

import goorm.athena.domain.user.entity.User;
import goorm.athena.global.jwt.util.JwtTokenizer;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtTokenizer jwtTokenizer;

    // 토큰 발급, 재발급 공통 로직
    @Transactional
    public String issueToken(User user, HttpServletResponse response){
        String refreshTokenValue = jwtTokenizer.createRefreshToken(
                user.getId(),
                user.getNickname(),
                user.getRole().name());

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshTokenValue)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return refreshTokenValue;
    }
}
