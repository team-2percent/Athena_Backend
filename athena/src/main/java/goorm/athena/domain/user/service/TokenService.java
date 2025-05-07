package goorm.athena.domain.user.service;

import goorm.athena.domain.user.entity.RefreshToken;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.RefreshTokenRepository;
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
    private final RefreshTokenRepository refreshTokenRepository;

    // 토큰 발급, 재발급 공통 로직
    @Transactional
    public String issueToken(User user, HttpServletResponse response, boolean saveEntity){
        String refreshTokenValue = jwtTokenizer.createRefreshToken(user.getId(), user.getEmail(), user.getRole().name());

        if (saveEntity) {
            RefreshToken refreshToken = RefreshToken.create(user, refreshTokenValue);
            refreshTokenRepository.save(refreshToken);
        }

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshTokenValue)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return refreshTokenValue;
    }
}
