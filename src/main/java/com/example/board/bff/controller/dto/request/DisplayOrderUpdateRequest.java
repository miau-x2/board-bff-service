package com.example.board.bff.controller.dto.request;

import jakarta.validation.constraints.Max;

public record DisplayOrderUpdateRequest(
        Long id,
        @Max(value = 255, message = "디스플레이 순서는 최대 255입니다.")
        int displayOrder) {

        public static DisplayOrderUpdateRequest empty() {
                return new DisplayOrderUpdateRequest(null, 0);
        }
}
