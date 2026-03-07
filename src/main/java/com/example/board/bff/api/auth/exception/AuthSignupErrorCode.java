package com.example.board.bff.api.auth.exception;

import org.jspecify.annotations.Nullable;

import java.util.Map;

public enum AuthSignupErrorCode {
    TOKEN_EXPIRED,
    TOKEN_INVALID,
    EMAIL_DOMAIN_NOT_ALLOWED,
    USERNAME_DUPLICATED,
    EMAIL_DUPLICATED,
    NICKNAME_DUPLICATED;

    private static final Map<String, AuthSignupErrorCode> CODE_MAP = Map.ofEntries(
            Map.entry("AUTH_EMAIL_400_001", TOKEN_EXPIRED),
            Map.entry("AUTH_EMAIL_400_002", TOKEN_INVALID),
            Map.entry("AUTH_EMAIL_400_003", EMAIL_DOMAIN_NOT_ALLOWED),
            Map.entry("AUTH_CREDENTIAL_409_001", USERNAME_DUPLICATED),
            Map.entry("AUTH_CREDENTIAL_409_002", EMAIL_DUPLICATED),
            Map.entry("AUTH_CREDENTIAL_409_003", NICKNAME_DUPLICATED)
    );

    public static @Nullable AuthSignupErrorCode from(String code) {
        return CODE_MAP.get(code);
    }
}
