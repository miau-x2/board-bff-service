package com.example.board.bff.security.handler;

import com.example.board.bff.api.auth.service.LogoutService;
import com.example.board.bff.commons.utils.SessionConst;
import com.example.board.bff.controller.dto.request.LogoutRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {
    private final LogoutService logoutService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, @Nullable Authentication authentication) {
        if(authentication == null) return;

        if(!(authentication.getPrincipal() instanceof Long memberId)) return;

        var session = request.getSession(false);
        if(session == null) return;

        var refreshToken = (String) session.getAttribute(SessionConst.REFRESH_TOKEN);
        if(refreshToken == null || refreshToken.isBlank()) return;
        try {
            logoutService.logout(new LogoutRequest(memberId, refreshToken));
            log.info("인증 서버 리프레시 토큰 삭제 엔드포인트 호출 성공");
        } catch (Exception e) {
            log.error("인증 서버 리프레시 토큰 삭제 엔드포인트 호출 실패: {}", e.getMessage(), e);
        }
    }
}
