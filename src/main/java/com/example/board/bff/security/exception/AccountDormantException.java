package com.example.board.bff.security.exception;

import org.springframework.security.core.AuthenticationException;

public class AccountDormantException extends AuthenticationException {
    public AccountDormantException(String message) {
        super(message);
    }
}
