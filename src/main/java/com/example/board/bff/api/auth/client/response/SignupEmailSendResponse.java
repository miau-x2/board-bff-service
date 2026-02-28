package com.example.board.bff.api.auth.client.response;

public record SignupEmailSendResponse(long otpValiditySeconds, long cooldownSeconds) {
}
