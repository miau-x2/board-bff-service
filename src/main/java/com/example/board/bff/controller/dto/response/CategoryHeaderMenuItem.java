package com.example.board.bff.controller.dto.response;

import java.util.List;

public record CategoryHeaderMenuItem(
        Long id,
        String name,
        String slug,
        int displayOrder,
        List<CategoryHeaderMenuItem> children) {
}
