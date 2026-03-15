package com.example.board.bff.api.post.exception;

import org.jspecify.annotations.Nullable;

import java.util.Map;

public enum PostCategoryAddErrorCode {
    DEPTH_EXCEEDED,
    PARENT_CATEGORY_NOT_FOUND,
    NAME_DUPLICATED,
    SLUG_DUPLICATED
    ;

    private static final Map<String, PostCategoryAddErrorCode> CODE_MAP = Map.ofEntries(
            Map.entry("POST_CATEGORY_400_001", DEPTH_EXCEEDED),
            Map.entry("POST_CATEGORY_404_001", PARENT_CATEGORY_NOT_FOUND),
            Map.entry("POST_CATEGORY_409_001", NAME_DUPLICATED),
            Map.entry("POST_CATEGORY_409_002", SLUG_DUPLICATED)
    );

    public static @Nullable PostCategoryAddErrorCode from(String code) {
        return CODE_MAP.get(code);
    }
}
