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
public class RefreshTokenCommandService {
    private final UserQueryService userQueryService;
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
        else {

            boolean isAccessTokenValid = jwtTokenizer.isValidAccessToken(accessToken);
            boolean isRefreshTokenValid = jwtTokenizer.isValidRefreshToken(refreshToken);

            // 2. Access, Refresh 둘 다 유효한 경우 → Access 토큰 그대로 사용, Refresh도 그대로
            if (isAccessTokenValid && isRefreshTokenValid) {
                Claims claims = jwtTokenizer.parseAccessToken(accessToken);
                Long userId = Long.parseLong(claims.getSubject());

                return RefreshTokenMapper.toRefreshTokenResponse(userId, accessToken, refreshToken);
            }


            // 3. Access는 만료, Refresh는 유효 → Access만 재발급
            else if (!isAccessTokenValid && isRefreshTokenValid) {
                Claims claims = jwtTokenizer.parseRefreshToken(refreshToken);
                Long userId = Long.valueOf(claims.getSubject());

                User user = userQueryService.getUser(userId);

                String newAccessToken = jwtTokenizer.createAccessToken(user.getId(), user.getNickname(), user.getRole().name());

                return RefreshTokenMapper.toRefreshTokenResponse(user.getId(), newAccessToken, refreshToken);
            }


            // 4. access는 유효, refresh는 만료 -> 로그아웃 상태로 변경
            // ( 이전에 refreshToken이 true인 경우를 모두 검증하였기에 accessToken만 조건 설정
            else if (isAccessTokenValid) {
                deleteRefreshToken(response);
                throw new CustomException(ErrorCode.REFRESHTOKEN_EXPIRED);
            } else {
                // 5. 둘 다 만료 → 로그인 필요
                deleteRefreshToken(response);
                throw new CustomException(ErrorCode.AUTH_TOKEN_EXPIRED);
            }
        }
    }
}
