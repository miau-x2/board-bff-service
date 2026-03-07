package com.example.board.bff.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "아이디 또는 비밀번호를 입력해주세요.")
        String username,
        @NotBlank(message = "아이디 또는 비밀번호를 입력해주세요.")
        String password)
{
    public static LoginRequest empty() {
        return new LoginRequest(null, null);
    }
}
