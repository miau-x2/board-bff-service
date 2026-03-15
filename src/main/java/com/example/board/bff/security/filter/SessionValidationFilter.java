package com.example.board.bff.security.filter;

import com.example.board.bff.commons.utils.SessionConst;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class SessionValidationFilter extends OncePerRequestFilter {
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var session = request.getSession(false);
        if(session == null) {
            filterChain.doFilter(request, response);
            return;
        }
        var sessionExpiresAt = (Instant) session.getAttribute(SessionConst.SESSION_EXPIRATION);
        if(sessionExpiresAt == null) {
            filterChain.doFilter(request, response);
            return;
        }
        if(Instant.now().isAfter(sessionExpiresAt)) {
            session.invalidate();
            SecurityContextHolder.clearContext();
            authenticationEntryPoint
                    .commence(request, response, new InsufficientAuthenticationException("세션이 만료되었습니다."));
            return;
        }
        filterChain.doFilter(request, response);
    }
}
