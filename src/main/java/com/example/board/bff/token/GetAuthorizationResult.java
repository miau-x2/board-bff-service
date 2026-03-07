package com.example.board.bff.token;

public sealed interface GetAuthorizationResult {
    record Success(String authorization) implements GetAuthorizationResult {}
    record Unauthorized() implements GetAuthorizationResult {}
    record SystemError() implements GetAuthorizationResult {}
}
