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
                .authorizeHttpRequests((authorize) -> {
                    authorize
                            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html") // Swagger UI 및 API 문서 경로 허용
                            .permitAll()
                            .anyRequest()
                            .authenticated();
                })
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll());

        return http.build();
    }
}