package com.example.board.bff.api.exception;

public class UnauthorizedTokenException extends RuntimeException {
    public UnauthorizedTokenException(String message) {
        super(message);
    }
}
