package goorm.athena.global.jwt.token;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private String token;
    private Object principal; // 로그인한 사용자의 id, email
    private Object credentials;

    public JwtAuthenticationToken(String email, String password){
        super(null);
        this.principal = email;
        this.credentials = password;
    }

    public JwtAuthenticationToken(Collection<? extends GrantedAuthority> authorities,
                                  Object principal, Object credentials){
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        this.setAuthenticated(true);
    }

    public JwtAuthenticationToken(String token) {
        super(null);
        this.token = token;
        this.setAuthenticated(false);
    }

    @Override
    public Object getCredentials(){
        return this.credentials;
    }

    @Override
    public Object getPrincipal(){
        return this.principal;
    }

    public void setToken(String accessToken) {
    }
}
