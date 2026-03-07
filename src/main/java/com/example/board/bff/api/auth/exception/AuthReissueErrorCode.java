package com.example.board.bff.api.auth.exception;

import org.jspecify.annotations.Nullable;

import java.util.Map;

public enum AuthReissueErrorCode {
    REFRESH_TOKEN_INVALID,
    TOKEN_REISSUE_TEMPORARILY_UNAVAILABLE;

    private static final Map<String, AuthReissueErrorCode> CODE_MAP = Map.ofEntries(
            Map.entry("AUTH_TOKEN_401_001", REFRESH_TOKEN_INVALID),
            Map.entry("AUTH_TOKEN_503_001", TOKEN_REISSUE_TEMPORARILY_UNAVAILABLE)
    );

    public static @Nullable AuthReissueErrorCode from(String code) {
        return CODE_MAP.get(code);
    }
}
