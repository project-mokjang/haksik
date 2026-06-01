package com.example.haksikmokjang.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/terms/**",
                                "/api/members/signup/user",
                                "/api/members/check-login-id",
                                "/api/members/check-email",
                                "/api/users/check-nickname",

                                // 정적 리소스 허용
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico"
                                ).permitAll() // /api/auth,terms로 시작하는 모든 요청은 인증 없이 통과
                        .anyRequest().authenticated() // 나머지는 다 막음
                );

        return http.build();
    }
}