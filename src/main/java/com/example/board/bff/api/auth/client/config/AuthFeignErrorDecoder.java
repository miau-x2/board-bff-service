package com.example.board.bff.api.auth.client.config;

import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class AuthFeignErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        return FeignException.errorStatus(methodKey, response);
    }
}
