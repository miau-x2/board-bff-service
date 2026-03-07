package com.example.board.bff.security.handler;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;

@Component
public class SafeRedirectAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private final LoginRedirectProperties loginRedirectProperties;

    public SafeRedirectAuthenticationSuccessHandler(LoginRedirectProperties loginRedirectProperties, DefaultRedirectProperties defaultRedirectProperties) {
        this.loginRedirectProperties = loginRedirectProperties;
        setAlwaysUseDefaultTargetUrl(false);
        setTargetUrlParameter("redirect");
        setDefaultTargetUrl(defaultRedirectProperties.defaultTargetUrl());
        setUseReferer(true);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        super.onAuthenticationSuccess(request, response, chain, authentication);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        var targetUrl = super.determineTargetUrl(request, response);

        if(isSafeTargetUrl(targetUrl)) {
            return targetUrl;
        }

        return getDefaultTargetUrl();
    }

    private boolean isSafeTargetUrl(String url) {
        if(url == null || url.isBlank()) {
            return false;
        }
        if(url.contains("\\") || url.contains("\r") || url.contains("\n")) {
            return false;
        }
        final URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException _) {
            return false;
        }
        // 절대 URL만 허용
        if(uri.getScheme() == null || uri.getHost() == null) {
            return false;
        }
        // allowed origin만 허용
        var origin = toOrigin(uri);
        if(!loginRedirectProperties.allowedOrigins().contains(origin)) {
            return false;
        }
        // 올바른 path만 허용
        // 올바른 path 예시: GET /login
        var path = uri.getPath();
        if(path == null || path.isBlank() || !path.startsWith("/")) {
            return false;
        }
        // 로그인 성공시 로그인 요청과 로그아웃 요청으로 리다이렉트 할 필요가 없으므로 제외
        if(path.equals("/login") || path.equals("/logout")) {
            return false;
        }
        return true;
    }

    private String toOrigin(URI uri) {
        String scheme = uri.getScheme().toLowerCase();
        String host = uri.getHost();
        int port = uri.getPort();

        boolean defaultPort =
                ("https".equals(scheme) && (port == -1 || port == 443)) ||
                        ("http".equals(scheme) && (port == -1 || port == 80));

        return defaultPort ? (scheme + "://" + host) : (scheme + "://" + host + ":" + port);
    }
}
