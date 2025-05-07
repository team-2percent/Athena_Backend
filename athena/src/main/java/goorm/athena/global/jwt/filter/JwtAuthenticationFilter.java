package goorm.athena.global.jwt.filter;

import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import goorm.athena.global.jwt.token.JwtAuthenticationToken;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
        try {
            token = getToken(request);
            if (StringUtils.hasText(token)) {
                getAuthentication(token);
            }
            filterChain.doFilter(request, response);
        }catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.AUTH_TOKEN_EXPIRED);
        } catch (JwtException e) {
            throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN);
        }catch (Exception e) {
            throw new CustomException(ErrorCode.AUTH_FAILED);
        }
    }

    private void getAuthentication(String token){
        if (!StringUtils.hasText(token)) {
            return;
        }
        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(token);
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authenticate);
    }

    private String getToken(HttpServletRequest request){
        String authorization = request.getHeader("Authorization");
        if(StringUtils.hasText(authorization) && authorization.startsWith("Bearer")){
            String[] arr = authorization.split(" ");
            return (arr.length > 1) ? arr[1] : null;
        }
        return null;
    }
}
