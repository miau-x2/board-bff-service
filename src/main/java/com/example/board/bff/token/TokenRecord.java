package com.example.board.bff.token;

import java.time.Instant;

public record TokenRecord(
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt,
        String tokenType)
{}
