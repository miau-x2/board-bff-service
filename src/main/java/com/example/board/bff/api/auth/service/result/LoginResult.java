package com.example.board.bff.api.auth.service.result;

import com.example.board.bff.controller.dto.response.LoginResponse;

public sealed interface LoginResult {
    record Success(LoginResponse response) implements LoginResult {}
    record BadCredentials(String message) implements LoginResult {}
    record AccountPending(String message) implements LoginResult {}
    record AccountDormant(String message) implements LoginResult {}
    record AccountWithdrawn(String message) implements LoginResult {}
    record SystemError(String message) implements LoginResult {}
}