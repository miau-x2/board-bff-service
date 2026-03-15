package com.example.board.bff.config;

import com.example.board.bff.api.exception.ProtectedResourceApiErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class ProtectedFeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new ProtectedResourceApiErrorDecoder();
    }
}
