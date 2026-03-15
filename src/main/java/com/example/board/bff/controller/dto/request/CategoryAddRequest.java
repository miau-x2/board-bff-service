package com.example.board.bff.controller.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryAddRequest(
        Long parentId,
        @NotBlank(message = "카테고리 이름을 입력해주세요.")
        @Size(min = 1, max = 50, message = "카테고리 이름은 1~50자입니다.")
        String name,
        @NotBlank(message = "카테고리 슬러그를 입력해주세요.")
        @Size(min = 1, max = 50, message = "카테고리 슬러그는 1~50자입니다.")
        String slug,
        @Max(value = 255, message = "디스플레이 순서는 최대 255입니다.")
        int displayOrder) {
    public static CategoryAddRequest empty() {
        return new CategoryAddRequest(null, "", "", 0);
    }
}
