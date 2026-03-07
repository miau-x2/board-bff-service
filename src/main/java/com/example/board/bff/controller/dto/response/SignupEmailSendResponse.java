package com.example.board.bff.controller.dto.response;

public record SignupEmailSendResponse(long otpValiditySeconds, long cooldownSeconds) {
}
