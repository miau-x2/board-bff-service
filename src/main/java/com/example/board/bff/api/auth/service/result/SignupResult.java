package com.example.board.bff.api.auth.service.result;

public sealed interface SignupResult {
    record Success() implements SignupResult {}
    record TokenExpired(String message) implements SignupResult {}
    record TokenInvalid(String message) implements SignupResult {}
    record EmailDomainNotAllowed(String message) implements SignupResult {}
    record UsernameDuplicated(String message) implements SignupResult {}
    record EmailDuplicated(String message) implements SignupResult {}
    record NicknameDuplicated(String message) implements SignupResult {}
    record SystemError(String message) implements SignupResult {}
}