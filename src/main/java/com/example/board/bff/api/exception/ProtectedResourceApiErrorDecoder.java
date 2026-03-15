package com.example.board.bff.api.exception;

import feign.Response;
import feign.codec.ErrorDecoder;

import java.util.Collection;
import java.util.Map;

public class ProtectedResourceApiErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String s, Response response) {
        if(response.status() != 401) {
            return defaultDecoder.decode(s, response);
        }

        var wwwAuthenticate = getHeader(response.headers(), "WWW-Authenticate");
        if(wwwAuthenticate != null &&
                wwwAuthenticate.contains("error=\"invalid_token\"") &&
                (
                        wwwAuthenticate.contains("error_description=\"Token invalid\"") ||
                        wwwAuthenticate.contains("error_description=\"Token expired\"")
                )) {
            return new UnauthorizedTokenException("토큰이 유효하지 않거나 만료되었습니다.");
        }

        return defaultDecoder.decode(s, response);
    }

    private String getHeader(Map<String, Collection<String>> headers, String header) {
        if(headers == null) {
            return null;
        }
        for (var headerEntry : headers.entrySet()) {
            if(headerEntry.getKey() != null && headerEntry.getKey().equalsIgnoreCase(header)) {
                var headerValues = headerEntry.getValue();
                if(headerValues == null || headerValues.isEmpty()) {
                    return null;
                }
                return headerValues.iterator().next();
            }
        }
        return null;
    }
}
