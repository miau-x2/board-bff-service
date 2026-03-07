package com.example.board.bff.config;

import com.example.board.bff.security.filter.CustomAuthenticationFilter;
import com.example.board.bff.security.filter.SessionValidationFilter;
import com.example.board.bff.security.handler.CustomAuthenticationFailureHandler;
import com.example.board.bff.security.handler.SafeRedirectAuthenticationSuccessHandler;
import com.example.board.bff.security.provider.CustomAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CustomAuthenticationProvider authenticationProvider,
            SessionValidationFilter sessionValidationFilter,
            CustomAuthenticationFilter authenticationFilter,
            SessionRegistry sessionRegistry,
            SecurityContextRepository securityContextRepository) {

        http.csrf(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/error",
                                "/login",
                                "/login/**",
                                "/signup",
                                "/signup/**",
                                "/favicon.ico",
                                "/assets/**",
                                "/hello"
                        ).permitAll()
                        .anyRequest()
                        .authenticated()
        );
        http.authenticationProvider(authenticationProvider);
        http.addFilterBefore(sessionValidationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAt(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                .accessDeniedHandler(((request, response, accessDeniedException) -> response.sendRedirect("/error/403")))
        );
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
        );
        http.sessionManagement(sessions -> sessions
                .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::changeSessionId)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .sessionRegistry(sessionRegistry)
        );
        http.securityContext(context -> context
                .securityContextRepository(securityContextRepository)
        );
        return http.build();
    }

    @Bean
    public CustomAuthenticationFilter customAuthenticationFilter(
            SessionTimeoutProperties sessionTimeoutProperties,
            AuthenticationManager authenticationManager,
            SafeRedirectAuthenticationSuccessHandler successHandler,
            CustomAuthenticationFailureHandler failureHandler,
            SecurityContextRepository securityContextRepository) {
        var customFilter = new CustomAuthenticationFilter(sessionTimeoutProperties);
        customFilter.setAuthenticationManager(authenticationManager);
        customFilter.setRequiresAuthenticationRequestMatcher(
                PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/login")
        );
        customFilter.setSecurityContextRepository(securityContextRepository);
        customFilter.setAuthenticationSuccessHandler(successHandler);
        customFilter.setAuthenticationFailureHandler(failureHandler);

        return customFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SessionRegistry sessionRegistry(RedisIndexedSessionRepository redisIndexedSessionRepository) {
        return new SpringSessionBackedSessionRegistry<>(redisIndexedSessionRepository);
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        var contextRepository = new HttpSessionSecurityContextRepository();
        contextRepository.setAllowSessionCreation(true);
        return contextRepository;
    }
}