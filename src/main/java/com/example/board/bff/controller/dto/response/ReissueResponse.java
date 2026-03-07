package com.example.board.bff.controller.dto.response;

import java.time.Instant;

public record ReissueResponse(
        Long memberId,
        String role,
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt,
        String tokenType
) {}