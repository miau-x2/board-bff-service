package com.example.board.bff.token;

public sealed interface GetAccessTokenResult {
    record Success(TokenRecord tokenRecord) implements GetAccessTokenResult {}
    record SessionInvalid() implements GetAccessTokenResult {}
    record SessionExpired() implements GetAccessTokenResult {}
    record RefreshTokenExpired() implements GetAccessTokenResult {}
    record ReissueRetryable() implements GetAccessTokenResult {}
    record ReissueFailed() implements GetAccessTokenResult {}
    record SystemError() implements GetAccessTokenResult {}
}
