package goorm.athena.global.jwt.filter;

import goorm.athena.domain.user.service.RefreshTokenService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import goorm.athena.global.jwt.token.JwtAuthenticationToken;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final AuthenticationManager authenticationManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException{
        String token = "";
        String refreshToken = getRefreshToken(request);

        // 리프레시 토큰 검증
        if(StringUtils.hasText(refreshToken)) {
            try {
                getAuthentication(refreshToken);
            } catch (ExpiredJwtException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("refreshToken is expired");
                deleteRefreshToken(response);
                return;
            }
        }

        String requestURI = request.getRequestURI();

        // refresh 요청은 accessToken 검증에서 제외
        if (requestURI.equals("/api/refreshToken/ReissueRefresh")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 액세스 토큰 검증
        token = getAccessToken(request);
        if (StringUtils.hasText(token)) {
            try {
                getAuthentication(token);
            } catch (ExpiredJwtException e){
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("""
                {
                  "code": "ACCESS_TOKEN_EXPIRED",
                  "message": "AccessToken is expired"
                }
                """);
                return;
            } catch (JwtException e){
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("The token is empty");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String getAccessToken(HttpServletRequest request){
        String authorization = request.getHeader("Authorization");
        if(StringUtils.hasText(authorization) && authorization.startsWith("Bearer")){
            String[] arr = authorization.split(" ");
            return (arr.length > 1) ? arr[1] : null;
        }
        return null;
    }

    private void getAuthentication(String token){
        if (!StringUtils.hasText(token)) {
            return;
        }
        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(token);
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authenticate);
    }

    private String getRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

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

}
