package com.example.board.bff.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/error",
                                "/signup",
                                "/signup/**",
                                "/favicon.ico",
                                "/assets/**"
                        ).permitAll()
                        .anyRequest().authenticated())
                .logout(logout -> logout.permitAll());
        return http.build();
    }
}
