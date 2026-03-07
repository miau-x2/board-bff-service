package com.example.board.bff.controller.dto.response;

public record AvailabilityResponse(boolean available, String message) {
    public static AvailabilityResponse available(String message) {
        return new AvailabilityResponse(true, message);
    }
    public static AvailabilityResponse unavailable(String message) {
        return new AvailabilityResponse(false, message);
    }
}
