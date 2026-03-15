package com.example.board.bff.api.post.service.result;

public sealed interface UpdateDisplayOrderResult {
    record Success() implements UpdateDisplayOrderResult {}
    record Unauthorized() implements UpdateDisplayOrderResult {}
    record NotFound(String message) implements UpdateDisplayOrderResult {}
    record SystemError(String message) implements UpdateDisplayOrderResult {}
}
