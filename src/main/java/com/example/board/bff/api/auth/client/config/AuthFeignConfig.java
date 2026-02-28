package com.example.board.bff.api.auth.client.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthFeignConfig {
    @Bean
    public ErrorDecoder authFeignErrorDecoder() {
        return new AuthFeignErrorDecoder();
    }
}
