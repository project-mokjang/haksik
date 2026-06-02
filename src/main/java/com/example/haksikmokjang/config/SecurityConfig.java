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

    // ... 상단 생략
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/signup", // 팩트: 브라우저가 화면을 띄우려면 이 주소가 무조건 열려있어야 합니다.
                                "/api/auth/**",
                                "/api/terms/**",
                                "/api/members/signup/user",
                                "/api/members/check-login-id",
                                "/api/members/check-email",
                                "/api/users/check-nickname",

                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "favicon.ico"
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}