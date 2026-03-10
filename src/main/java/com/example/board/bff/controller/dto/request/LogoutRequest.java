package com.example.board.bff.controller.dto.request;

public record LogoutRequest(Long memberId, String refreshToken) {
}
