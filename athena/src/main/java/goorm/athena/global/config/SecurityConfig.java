package goorm.athena.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // 임시로 csrf 비활성화
                .authorizeHttpRequests((authorize) -> {
                    authorize
                            .requestMatchers("/api/**","/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html") // Swagger UI 및 API 문서 경로 허용
                            .permitAll()
                            .anyRequest()
                            .authenticated();
                })
                /*
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll());
                 */.formLogin().disable();
        return http.build();
    }
}