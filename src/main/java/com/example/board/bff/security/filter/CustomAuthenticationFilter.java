package com.example.board.bff.security.filter;

import com.example.board.bff.security.authentication.CustomAuthenticationToken;
import com.example.board.bff.commons.utils.SessionConst;
import com.example.board.bff.config.SessionTimeoutProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.Instant;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final SessionTimeoutProperties sessionTimeoutProperties;

    public CustomAuthenticationFilter(SessionTimeoutProperties sessionTimeoutProperties) {
        this.sessionTimeoutProperties = sessionTimeoutProperties;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (!"POST".equals(request.getMethod())) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        } else {
            String username = obtainUsername(request);
            username = username != null ? username.trim() : "";
            String password = obtainPassword(request);
            password = password != null ? password : "";
            var authRequest = CustomAuthenticationToken.unauthenticated(username, password);
            setDetails(request, authRequest);
            return getAuthenticationManager().authenticate(authRequest);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        if(!(authResult instanceof CustomAuthenticationToken auth)) {
            unsuccessfulAuthentication(request, response, new AuthenticationServiceException("Authentication must be CustomAuthenticationToken"));
            return;
        }
        var session = request.getSession();
        session.setAttribute(SessionConst.MEMBER_ID, authResult.getPrincipal());
        session.setAttribute(SessionConst.ACCESS_TOKEN, auth.getTokenPair().accessToken());
        session.setAttribute(SessionConst.ACCESS_TOKEN_EXPIRATION, auth.getTokenPair().accessTokenExpiresAt());
        session.setAttribute(SessionConst.REFRESH_TOKEN, auth.getTokenPair().refreshToken());
        session.setAttribute(SessionConst.REFRESH_TOKEN_EXPIRATION, auth.getTokenPair().refreshTokenExpiresAt());
        session.setAttribute(SessionConst.SESSION_EXPIRATION, Instant.now().plus(sessionTimeoutProperties.absoluteTimeout()));
        session.setAttribute(SessionConst.TOKEN_TYPE,auth.getTokenPair().type());

        var cleanAuth = UsernamePasswordAuthenticationToken.authenticated(auth.getPrincipal(), null, auth.getAuthorities());
        cleanAuth.setDetails(authenticationDetailsSource.buildDetails(request));
        super.successfulAuthentication(request, response, chain, cleanAuth);
    }
}
