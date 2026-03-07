package com.example.board.bff.api.auth.exception;

import org.jspecify.annotations.Nullable;

import java.util.Map;

public enum AuthCommonErrorCode {
    INTERNAL_SERVER_ERROR;

    private static final Map<String, AuthCommonErrorCode> CODE_MAP = Map.ofEntries(
            Map.entry("AUTH_COMMON_500_001", INTERNAL_SERVER_ERROR)
    );

    public static @Nullable AuthCommonErrorCode from(String code) {
        return CODE_MAP.get(code);
    }
}
