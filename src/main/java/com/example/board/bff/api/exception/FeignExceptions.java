package com.example.board.bff.api.exception;

import com.example.board.bff.commons.response.ApiResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeignExceptions {
    private final JsonMapper jsonMapper;

    public Optional<ApiResponse<Void>> extractErrorResponse(FeignException e) {
        log.warn("content: {}", e.contentUTF8());
        return Optional.ofNullable(e.contentUTF8())
                .filter(jsonBody -> !jsonBody.isBlank())
                .flatMap(jsonBody -> {
                    try {
                        return Optional.of(jsonMapper.readValue(jsonBody, new TypeReference<ApiResponse<Void>>() {}));
                    } catch (JacksonException _) {
                        return Optional.empty();
                    }
                });
    }
}
