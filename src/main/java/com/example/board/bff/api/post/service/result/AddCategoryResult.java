package com.example.board.bff.api.post.service.result;

public sealed interface AddCategoryResult {
    record Success() implements AddCategoryResult {}
    record Unauthorized() implements AddCategoryResult {}
    record DepthExceeded(String message) implements AddCategoryResult {}
    record ParentNotFound(String message) implements AddCategoryResult {}
    record NameDuplicated(String message) implements AddCategoryResult {}
    record SlugDuplicated(String message) implements AddCategoryResult {}
    record SystemError(String message) implements AddCategoryResult {}
}
