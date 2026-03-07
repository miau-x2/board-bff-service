package com.example.board.bff.config.retry;

public class RetryableReissueException extends RetryableRemoteException {
    public RetryableReissueException(String message) {
        super(message);
    }
}
