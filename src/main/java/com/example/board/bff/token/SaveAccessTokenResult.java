package com.example.board.bff.token;

public sealed interface SaveAccessTokenResult {
    record Success() implements SaveAccessTokenResult {}
    record SessionInvalid() implements SaveAccessTokenResult {}
    record SessionExpired() implements SaveAccessTokenResult {}
    record SystemError() implements SaveAccessTokenResult {}
}
