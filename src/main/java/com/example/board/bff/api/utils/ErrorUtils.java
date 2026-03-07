package com.example.board.bff.api.utils;

import org.springframework.validation.BindingResult;

public final class ErrorUtils {
    private ErrorUtils() {}

    public static void addFieldError(BindingResult bindingResult, String field, String errorCode, String defaultMessage) {
        bindingResult.rejectValue(field, errorCode, defaultMessage);
    }

    public static void addGlobalError(BindingResult bindingResult, String errorCode, String defaultMessage) {
        bindingResult.reject(errorCode, defaultMessage);
    }

    public static String addFieldErrorReturnView(BindingResult bindingResult, String field, String errorCode, String defaultMessage, String viewName) {
        addFieldError(bindingResult, field, errorCode, defaultMessage);
        return viewName;
    }

    public static String addGlobalErrorReturnView(BindingResult bindingResult, String errorCode, String defaultMessage, String viewName) {
        addGlobalError(bindingResult, errorCode, defaultMessage);
        return viewName;
    }

    public static String handleUnknownErrorCode(BindingResult bindingResult, String viewName) {
        return addGlobalErrorReturnView(bindingResult, "system", "현재 요청을 처리할 수 없습니다. 잠시 후 다시 시도해주세요.", viewName);
    }
}
