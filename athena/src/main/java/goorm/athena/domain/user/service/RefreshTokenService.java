package goorm.athena.domain.user.service;

import goorm.athena.domain.user.dto.response.RefreshTokenResponse;
import goorm.athena.domain.user.entity.RefreshToken;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.mapper.RefreshTokenMapper;
import goorm.athena.domain.user.repository.RefreshTokenRepository;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import goorm.athena.global.jwt.util.JwtTokenizer;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenizer jwtTokenizer;
    private final TokenService tokenService;

    @Transactional
    public void deleteRefreshToken(String refreshToken){
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByValue(refreshToken);

        if(existingToken.isPresent()){
            refreshTokenRepository.delete(existingToken.get());
        } else {
            throw new CustomException(ErrorCode.REFRESHTOKEN_NOT_FOUND);
        }
    }

    @Transactional
    public RefreshTokenResponse refreshAccessToken(String refreshTokenValue){
        // 리프레시 토큰 확인
        RefreshToken refreshToken = refreshTokenRepository.findByValue(refreshTokenValue)
                .orElseThrow(() -> new CustomException(ErrorCode.REFRESHTOKEN_NOT_FOUND));

        Claims claims = jwtTokenizer.parseRefreshToken(refreshTokenValue);
        Long userId = Long.valueOf((Integer) claims.get("userId"));

        User user = userService.getUser(userId);

        String newAccessToken = jwtTokenizer.createAccessToken(user.getId(), claims.getSubject(), claims.get("roles", String.class));

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

        refreshTokenRepository.deleteAllByUser(user);

        String accessToken = jwtTokenizer.createAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String newRefreshToken = tokenService.issueToken(user, response);

        return RefreshTokenMapper.toRefreshTokenResponse(user.getId(), accessToken, newRefreshToken);
    }
}
