package com.example.haksikmokjang.config;

import com.example.haksikmokjang.security.LoginFailureHandler;
import com.example.haksikmokjang.security.LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final DataSource dataSource;

    private static final String[] STATIC_URLS = {
            "/css/**",
            "/js/**",
            "/images/**",
            "/favicon.ico"
    };

    private static final String[] PUBLIC_VIEW_URLS = {
            "/",
            "api/view/main",
            "/api/view/login",
            "/api/view/signup-choice",
            "/api/view/signup-user",
            "/api/view/signup-owner",
            "/api/view/find-id",
            "/api/view/find-pw",
            "/api/view/reset-pw"
    };

    private static final String[] PUBLIC_API_URLS = {
            "/api/auth/email/**",
            "/api/auth/login",
            "/api/auth/logout",

            "/api/terms/**",

            "/api/members/check-login-id",
            "/api/members/check-school-email",
            "/api/members/check-nickname",
            "/api/members/check-business-number",
            "/api/members/signup/user",
            "/api/members/signup/owner"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(STATIC_URLS).permitAll()
                        .requestMatchers(PUBLIC_VIEW_URLS).permitAll()
                        .requestMatchers(PUBLIC_API_URLS).permitAll()

                        .requestMatchers("/api/view/user/**").hasRole("USER")
                        .requestMatchers("/api/view/owner/**").hasRole("OWNER")
                        .requestMatchers("/api/view/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/api/view/login")
                        .loginProcessingUrl("/api/auth/login")
                        .usernameParameter("loginId")
                        .passwordParameter("password")
                        .successHandler(loginSuccessHandler)
                        .failureHandler(loginFailureHandler)
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key("haksik-mokjang-remember-key")
                        .rememberMeParameter("rememberMe")
                        .tokenValiditySeconds(60 * 60 * 24 * 14)
                        .tokenRepository(persistentTokenRepository())
                )

                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessUrl("/api/view/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                );

        return http.build();
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl repository = new JdbcTokenRepositoryImpl();
        repository.setDataSource(dataSource);
        return repository;
    }
}