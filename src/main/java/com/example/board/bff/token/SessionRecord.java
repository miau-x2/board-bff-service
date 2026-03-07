package com.example.board.bff.token;

import java.time.Instant;

public record SessionRecord(
        Long memberId,
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt,
        String tokenType,
        Instant sessionExpiresAt)
{}
