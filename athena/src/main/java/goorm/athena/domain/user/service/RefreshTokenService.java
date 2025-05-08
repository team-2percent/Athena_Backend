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

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final UserService userService;
    private final JwtTokenizer jwtTokenizer;

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
    public RefreshTokenResponse reissueToken(String accessToken, String refreshToken, HttpServletResponse response){
        // 1. RefreshToken이 아예 없다면
        if(refreshToken == null || refreshToken.isEmpty()){
            throw new CustomException(ErrorCode.REFRESHTOKEN_NOT_FOUND);
        }

        boolean isAccessTokenValid = jwtTokenizer.isValidAccessToken(accessToken);
        boolean isRefreshTokenValid = jwtTokenizer.isValidRefreshToken(refreshToken);

        // 2. Access, Refresh 둘 다 유효한 경우 → Access 토큰 그대로 사용, Refresh도 그대로
        if (isAccessTokenValid && isRefreshTokenValid) {
            Claims claims = jwtTokenizer.parseAccessToken(accessToken);
            Long userId = Long.parseLong(claims.getSubject());

            return RefreshTokenMapper.toRefreshTokenResponse(userId, accessToken, refreshToken);
        }


        // 3. Access는 만료, Refresh는 유효 → Access만 재발급
        if (!isAccessTokenValid && isRefreshTokenValid) {
            Claims claims = jwtTokenizer.parseRefreshToken(refreshToken);
            Long userId = Long.valueOf(claims.getSubject());

            User user = userService.getUser(userId);
            if (user == null) throw new CustomException(ErrorCode.USER_NOT_FOUND);

            String newAccessToken = jwtTokenizer.createAccessToken(user.getId(), user.getNickname(), user.getRole().name());

            return RefreshTokenMapper.toRefreshTokenResponse(user.getId(), newAccessToken, refreshToken);
        }


        // 4. Access는 유효, Refresh는 만료 → Refresh만 재발급
        if (isAccessTokenValid && !isRefreshTokenValid) {
            deleteRefreshToken(response);
            throw new CustomException(ErrorCode.REFRESHTOKEN_EXPIRED);
        }

        // 5. 둘 다 만료 → 로그인 필요
        deleteRefreshToken(response);
        throw new CustomException(ErrorCode.AUTH_TOKEN_EXPIRED);
    }
}
