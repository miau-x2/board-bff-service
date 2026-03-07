package com.example.board.bff.api.auth.exception;

import org.jspecify.annotations.Nullable;

import java.util.Map;

public enum AuthLoginErrorCode {
    BAD_CREDENTIALS,
    ACCOUNT_PENDING,
    ACCOUNT_DORMANT,
    ACCOUNT_WITHDRAWN;

    private static final Map<String, AuthLoginErrorCode> CODE_MAP = Map.ofEntries(
            Map.entry("AUTH_LOGIN_401_001", BAD_CREDENTIALS),
            Map.entry("AUTH_LOGIN_403_001", ACCOUNT_PENDING),
            Map.entry("AUTH_LOGIN_403_002", ACCOUNT_DORMANT),
            Map.entry("AUTH_LOGIN_403_003", ACCOUNT_WITHDRAWN)
    );

    public static @Nullable AuthLoginErrorCode from(String code) {
        return CODE_MAP.get(code);
    }
}
