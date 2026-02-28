package com.example.board.bff.api.auth.client.response;

import java.util.Map;
import java.util.Optional;

public enum AuthServiceErrorCode {
    TOKEN_EXPIRED,
    TOKEN_INVALID,
    EMAIL_DOMAIN_NOT_ALLOWED,
    USERNAME_DUPLICATED,
    EMAIL_DUPLICATED,
    NICKNAME_DUPLICATED,
    INTERNAL_SERVER_ERROR
    ;

    private static final Map<String, AuthServiceErrorCode> CODE_MAP = Map.of(
            "AUTH_EMAIL_400_001", TOKEN_EXPIRED,
            "AUTH_EMAIL_400_002", TOKEN_INVALID,
            "AUTH_EMAIL_400_003", EMAIL_DOMAIN_NOT_ALLOWED,
            "AUTH_CREDENTIAL_409_001", USERNAME_DUPLICATED,
            "AUTH_CREDENTIAL_409_002", EMAIL_DUPLICATED,
            "AUTH_CREDENTIAL_409_003", NICKNAME_DUPLICATED,
            "AUTH_COMMON_500_001", INTERNAL_SERVER_ERROR
    );

    public static Optional<AuthServiceErrorCode> fromCode(String code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }
}
