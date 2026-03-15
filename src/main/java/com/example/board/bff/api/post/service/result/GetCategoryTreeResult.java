package com.example.board.bff.api.post.service.result;

import com.example.board.bff.controller.dto.response.CategoryTreeResponse;

public sealed interface GetCategoryTreeResult {
    record Success(CategoryTreeResponse response) implements GetCategoryTreeResult {}
    record Unauthorized() implements GetCategoryTreeResult {}
    record SystemError(String message) implements GetCategoryTreeResult {}
}
