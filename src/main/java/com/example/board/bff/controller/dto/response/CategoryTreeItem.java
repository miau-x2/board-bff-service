package com.example.board.bff.controller.dto.response;

import java.util.List;

public record CategoryTreeItem(
        Long id,
        String name,
        String slug,
        int displayOrder,
        List<CategoryTreeItem> children) {
}
