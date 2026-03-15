package com.example.board.bff.token;

import com.example.board.bff.commons.utils.SessionConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionTokenContextRepository {
    private final RedisIndexedSessionRepository sessionRepository;

    public Optional<SessionRecord> findById(String sessionId) {
        var session = sessionRepository.findById(sessionId);
        if(session == null) {
            return Optional.empty();
        }
        var sessionExpiresAt = (Instant) session.getAttribute(SessionConst.SESSION_EXPIRATION);
        if(sessionExpiresAt == null || Instant.now().isAfter(sessionExpiresAt)) {
            return Optional.empty();
        }
        return Optional.of(new SessionRecord(
                session.getAttribute(SessionConst.MEMBER_ID),
                session.getAttribute(SessionConst.ACCESS_TOKEN),
                session.getAttribute(SessionConst.ACCESS_TOKEN_EXPIRATION),
                session.getAttribute(SessionConst.REFRESH_TOKEN),
                session.getAttribute(SessionConst.REFRESH_TOKEN_EXPIRATION),
                session.getAttribute(SessionConst.TOKEN_TYPE),
                sessionExpiresAt
        ));
    }

    public SaveAccessTokenResult save(String sessionId, TokenRecord tokenRecord) {
        var session = sessionRepository.findById(sessionId);
        if(session == null) {
            return new SaveAccessTokenResult.SessionInvalid();
        }
        var sessionExpiresAt = (Instant) session.getAttribute(SessionConst.SESSION_EXPIRATION);
        if(sessionExpiresAt == null || Instant.now().isAfter(sessionExpiresAt)) {
            return new SaveAccessTokenResult.SessionExpired();
        }

        try {
            session.setAttribute(SessionConst.ACCESS_TOKEN, tokenRecord.accessToken());
            session.setAttribute(SessionConst.ACCESS_TOKEN_EXPIRATION, tokenRecord.accessTokenExpiresAt());
            session.setAttribute(SessionConst.REFRESH_TOKEN, tokenRecord.refreshToken());
            session.setAttribute(SessionConst.REFRESH_TOKEN_EXPIRATION, tokenRecord.refreshTokenExpiresAt());
            session.setAttribute(SessionConst.TOKEN_TYPE, tokenRecord.tokenType());
            sessionRepository.save(session);
            return new SaveAccessTokenResult.Success();
        } catch (Exception e) {
            log.error("세션: {}, 액세스 토큰 저장 실패: {}", sessionId, e.getMessage(), e);
            return new SaveAccessTokenResult.SystemError();
        }
    }
}

