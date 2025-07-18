package goorm.athena.global.jwt.provider;

import goorm.athena.domain.user.entity.Role;
import goorm.athena.global.jwt.token.JwtAuthenticationToken;
import goorm.athena.global.jwt.util.JwtTokenizer;
import goorm.athena.global.jwt.util.LoginInfoDto;
import goorm.athena.global.jwt.util.LoginMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {
    private final JwtTokenizer jwtTokenizer;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof JwtAuthenticationToken)) {
            return null;
        }
        JwtAuthenticationToken authenticationToken = (JwtAuthenticationToken) authentication;

        // 토큰 유효성 검증
        jwtTokenizer.isValidAccessToken(authenticationToken.getToken());

        Claims claims = jwtTokenizer.parseAccessToken(authenticationToken.getToken());
        Long userId = Long.parseLong(claims.getSubject());
        String nickname = claims.get("nickname", String.class);
        List<GrantedAuthority> authorities = getGrantedAuthorities(claims);

        String roleStr = authorities.isEmpty() ? "ROLE_USER" : authorities.getFirst().getAuthority();
        Role role = Role.valueOf(roleStr);
        LoginInfoDto loginInfo = LoginMapper.toLoginInfo(userId, nickname, role);

        return new JwtAuthenticationToken(authorities, loginInfo, null);
    }

    private List<GrantedAuthority> getGrantedAuthorities(Claims claims){
        String roles = (String) claims.get("role");
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(() -> roles);
        return authorities;
    }

    @Override
    public boolean supports(Class<?> authentication){
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
