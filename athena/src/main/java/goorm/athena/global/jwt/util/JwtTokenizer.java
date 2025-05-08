package goorm.athena.global.jwt.util;

import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenizer {
    private final byte[] accessSecret;
    private final byte[] refreshSecret;

    public final static Long ACCESS_TOKEN_EXPIRE_COUNT = 30 * 60 * 1000L;
    public final static Long REFRESH_TOKEN_EXPIRE_COUNT = 7 * 24 * 60 * 60 * 1000L;

    public JwtTokenizer(@Value("${jwt.secretKey}") String accessSecret,
                        @Value("${jwt.refreshKey}") String refreshSecret){
        this.accessSecret = accessSecret.getBytes(StandardCharsets.UTF_8);
        this.refreshSecret = refreshSecret.getBytes(StandardCharsets.UTF_8);
    }

    // 토큰 생성 템플릿
    public String createToken(Long id, String nickname, String role,
                              Long expire, byte[] secretKey){
        Claims claims = Jwts.claims()
                .subject(String.valueOf(id))
                .add("role", role)
                .add("nickname", nickname)
                .build();

        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expire))
                .signWith(Keys.hmacShaKeyFor(secretKey))
                .compact();
    }

    public String createAccessToken(Long id, String nickname, String roles){
        return createToken(id, nickname, roles, ACCESS_TOKEN_EXPIRE_COUNT, accessSecret);
    }

    public String createRefreshToken(Long id, String nickname, String roles){
        return createToken(id, nickname, roles, REFRESH_TOKEN_EXPIRE_COUNT, refreshSecret);
    }

    public Claims parseToken(String token, byte[] secretKey){
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims parseAccessToken(String accessToken){
        return parseToken(extractBearerToken(accessToken), accessSecret);
    }

    public Claims parseRefreshToken(String refreshToken){
        return parseToken(extractBearerToken(refreshToken), refreshSecret);
    }

    private String extractBearerToken(String headerValue){
        if(headerValue != null && headerValue.startsWith("Bearer ")){
            return headerValue.substring(7);
        }
        return headerValue;
    }

    public Long getUserIdFromToken(String token){
        String[] tokenArr = token.split(" ");
        token = tokenArr[1];
        Claims claims = parseToken(token, accessSecret);
        return Long.parseLong(claims.getSubject());
    }

    public boolean isValidAccessToken(String accessToken) {
        return validateSilently(extractBearerToken(accessToken), accessSecret);
    }

    public boolean isValidRefreshToken(String refreshToken) {
        return validate(extractBearerToken(refreshToken), refreshSecret);
    }

    private boolean validate(String token, byte[] secretKey) {
        try {
            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secretKey))
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.AUTH_TOKEN_EXPIRED);
        } catch (UnsupportedJwtException e) {
            throw new CustomException(ErrorCode.AUTH_UNSUPPORTED_TOKEN);
        } catch (MalformedJwtException e){
            throw new CustomException(ErrorCode.AUTH_MALFORMED_TOKEN);
        } catch (SignatureException e) {
            throw new CustomException(ErrorCode.AUTH_INVALID_SIGNATURE);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.AUTH_EMPTY_TOKEN);
        } catch (JwtException e) {
            throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AUTH_FAILED);
        }
    }

    private boolean validateSilently(String token, byte[] secretKey) {
        try {
            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secretKey))
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
