package com.example.board.bff.commons.exception;

import com.example.board.bff.api.exception.FeignExceptions;
import com.example.board.bff.commons.response.ApiCode;
import com.example.board.bff.commons.response.ApiResponse;
import com.example.board.bff.commons.response.CommonErrorCode;
import feign.FeignException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final FeignExceptions feignExceptions;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        var errors = new ArrayList<FieldValidationError>();

        for (var fieldError : e.getBindingResult().getFieldErrors()) {
            var field = fieldError.getField();
            var message = fieldError.getDefaultMessage();
            if (message != null) {
                errors.add(new FieldValidationError(field, message));
            }
        }

        var message = errors.stream()
                .map(FieldValidationError::message)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(CommonErrorCode.INPUT_INVALID.getMessage());
        return handleValidationError(CommonErrorCode.INPUT_INVALID, message, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolationException(ConstraintViolationException e) {
        var errors = e.getConstraintViolations().stream()
                .map(violation -> new FieldValidationError(extractField(violation.getPropertyPath().toString()), violation.getMessage()))
                .toList();

        var message = errors.stream()
                .map(FieldValidationError::message)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(CommonErrorCode.INPUT_INVALID.getMessage());
        return handleValidationError(CommonErrorCode.INPUT_INVALID, message, errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        var errors = new ArrayList<FieldValidationError>();

        for (var result : e.getParameterValidationResults()) {
            var field = result.getMethodParameter().getParameterName();
            for (var error : result.getResolvableErrors()) {
                var message = error.getDefaultMessage();
                if (message != null) {
                    errors.add(new FieldValidationError(field, message));
                }
            }
        }

        var message = errors.stream()
                .map(FieldValidationError::message)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(CommonErrorCode.INPUT_INVALID.getMessage());
        return handleValidationError(CommonErrorCode.INPUT_INVALID, message, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMalformed() {
        return handleError(CommonErrorCode.REQUEST_MALFORMED);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch() {
        return handleError(CommonErrorCode.REQUEST_MALFORMED);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiResponse<Void>> handleFeignException(FeignException e) {
        int status = e.status() > 0 ? e.status() : 500;
        log.error("다운스트림 서비스 예외 발생, status: {} message: {}", e.status(), e.getMessage());
        var builder = ResponseEntity.status(status);

        if (status == 429) {
            var headers = e.responseHeaders();
            if (headers != null) {
                headers.forEach((name, values) -> {
                    if ("retry-after".equalsIgnoreCase(name) && values != null && !values.isEmpty()) {
                        builder.header("Retry-After", values.iterator().next());
                    }
                });
            }
        }
        var body = feignExceptions.extractErrorResponse(e)
                .orElseGet(() -> ApiResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR));
        return builder.body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("처리 되지 않은 예외 발생: {}", e.getMessage(), e);
        return handleError(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> handleError(ApiCode code) {
        return ResponseEntity.status(code.getHttpStatus()).body(ApiResponse.error(code));
    }

    private ResponseEntity<ApiResponse<Object>> handleValidationError(ApiCode code, String message, List<FieldValidationError> errors) {
        var payload = new ValidationErrorPayload(errors);
        return ResponseEntity
                .status(code.getHttpStatus())
                .body(new ApiResponse<>(false, code.getCode(), message, payload));
    }

    private String extractField(String propertyPath) {
        if (propertyPath == null || propertyPath.isBlank()) {
            return null;
        }
        var path = propertyPath.trim();
        var idx = path.lastIndexOf('.');
        if (idx < 0 || idx == path.length() - 1) {
            return path;
        }
        return path.substring(idx + 1);
    }

    private record ValidationErrorPayload(List<FieldValidationError> errors) {}
    private record FieldValidationError(String field, String message) {}
}
