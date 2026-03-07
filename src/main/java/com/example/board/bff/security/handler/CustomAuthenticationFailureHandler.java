package com.example.board.bff.security.handler;

import com.example.board.bff.commons.utils.FlashMapKey;
import com.example.board.bff.security.exception.AccountDormantException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private static final String DEFAULT_SYSTEM_ERROR_MESSAGE = "현재 요청을 처리할 수 없습니다. 잠시 후 다시 시도해주세요.";

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        var flashMap = getFlashMap(exception);

        var flashMapManager = new SessionFlashMapManager();
        flashMapManager.saveOutputFlashMap(flashMap, request, response);

        var redirect = request.getParameter("redirect");
        var target = "/login";
        if(redirect != null && !redirect.isBlank()) {
            target = "/login?redirect=" + URLEncoder.encode(request.getParameter("redirect"), StandardCharsets.UTF_8);
        }
        getRedirectStrategy().sendRedirect(request, response, target);
    }

    private @NonNull FlashMap getFlashMap(AuthenticationException exception) {
        return switch (exception) {
            case BadCredentialsException e -> process(FlashMapKey.LOGIN_FIELD_ERROR_MESSAGE, e.getMessage());
            case AccountDormantException e -> process(FlashMapKey.LOGIN_FIELD_ERROR_MESSAGE, e.getMessage());
            case AuthenticationServiceException e -> process(FlashMapKey.LOGIN_GLOBAL_ERROR_MESSAGE, e.getMessage());
            default -> process(FlashMapKey.LOGIN_GLOBAL_ERROR_MESSAGE, DEFAULT_SYSTEM_ERROR_MESSAGE);
        };
    }

    private @NonNull FlashMap process(String key, String message) {
        var flashMap = new FlashMap();
        flashMap.put(key, message);
        return flashMap;
    }
}
