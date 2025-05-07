package goorm.athena.global.jwt.util;

import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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
    public String createToken(Long id, String email, String role,
                              Long expire, byte[] secretKey){
        Claims claims = Jwts.claims()
                .subject(email)
                .add("role", role)
                .add("userId", id)
                .build();

        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expire))
                .signWith(Keys.hmacShaKeyFor(secretKey))
                .compact();
    }

    public String createAccessToken(Long id, String email, String roles){
        return createToken(id, email, roles, ACCESS_TOKEN_EXPIRE_COUNT, accessSecret);
    }

    public String createRefreshToken(Long id, String email, String roles){
        return createToken(id, email, roles, REFRESH_TOKEN_EXPIRE_COUNT, refreshSecret);
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

    public Long getMemberIdFromToken(String token){
        String[] tokenArr = token.split(" ");
        token = tokenArr[1];
        Claims claims = parseToken(token, accessSecret);
        return Long.valueOf((Integer)claims.get("userId"));
    }

    public boolean validateToken(String token){
        try{
            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(accessSecret))
                    .build()
                    .parse(token);

            return true;
        } catch (ExpiredJwtException e){
            throw new CustomException(ErrorCode.AUTH_TOKEN_EXPIRED);
        } catch (JwtException e){
            throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN);
        } catch (Exception e){
            throw new CustomException(ErrorCode.AUTH_FAILED);
        }
    }


}
