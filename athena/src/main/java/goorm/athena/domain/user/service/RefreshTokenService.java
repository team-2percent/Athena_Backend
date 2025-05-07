package goorm.athena.domain.user.service;

import goorm.athena.domain.user.dto.response.RefreshTokenResponse;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.mapper.RefreshTokenMapper;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import goorm.athena.global.jwt.util.JwtTokenizer;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final UserService userService;
    private final JwtTokenizer jwtTokenizer;
    private final TokenService tokenService;

    @Transactional
    public void deleteRefreshToken(HttpServletResponse response){
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

    }

    @Transactional
    public RefreshTokenResponse refreshAccessToken(String refreshTokenValue){
        Claims claims = jwtTokenizer.parseRefreshToken(refreshTokenValue);
        Long userId = Long.valueOf((Integer) claims.get("userId"));

        User user = userService.getUser(userId);

        String newAccessToken = jwtTokenizer.createAccessToken(user.getId(), claims.getSubject(), claims.get("role", String.class));

        return new RefreshTokenResponse(userId, newAccessToken, refreshTokenValue);
    }

    @Transactional
    public RefreshTokenResponse reissueToken(String refreshToken, HttpServletResponse response){
        if(refreshToken == null || refreshToken.isEmpty()){
            throw new CustomException(ErrorCode.REFRESHTOKEN_NOT_FOUND);
        }

        Claims claims = jwtTokenizer.parseRefreshToken(refreshToken);
        Long userId = Long.valueOf((Integer) claims.get("userId"));

        User user = userService.getUser(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        String accessToken = jwtTokenizer.createAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String newRefreshToken = tokenService.issueToken(user, response);

        return RefreshTokenMapper.toRefreshTokenResponse(user.getId(), accessToken, newRefreshToken);
    }
}
