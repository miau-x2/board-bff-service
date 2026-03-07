package com.example.board.bff.token;

import com.example.board.bff.commons.utils.SessionConst;
import lombok.RequiredArgsConstructor;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

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

    public boolean save(String sessionId, TokenRecord tokenRecord) {
        var session = sessionRepository.findById(sessionId);
        if(session == null) {
            return false;
        }
        var sessionExpiresAt = (Instant) session.getAttribute(SessionConst.SESSION_EXPIRATION);
        if(sessionExpiresAt == null || Instant.now().isAfter(sessionExpiresAt)) {
            return false;
        }

        session.setAttribute(SessionConst.ACCESS_TOKEN, tokenRecord.accessToken());
        session.setAttribute(SessionConst.ACCESS_TOKEN_EXPIRATION, tokenRecord.accessTokenExpiresAt());
        session.setAttribute(SessionConst.REFRESH_TOKEN, tokenRecord.refreshToken());
        session.setAttribute(SessionConst.REFRESH_TOKEN_EXPIRATION, tokenRecord.refreshTokenExpiresAt());
        session.setAttribute(SessionConst.TOKEN_TYPE, tokenRecord.tokenType());
        sessionRepository.save(session);
        return true;
    }
}

