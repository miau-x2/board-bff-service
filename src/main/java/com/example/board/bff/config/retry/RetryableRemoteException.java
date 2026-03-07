package com.example.board.bff.config.retry;

public class RetryableRemoteException extends RuntimeException {
    public RetryableRemoteException(String message) {
        super(message);
    }
}
