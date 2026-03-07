package com.example.board.bff.config.retry;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryListener;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
public class RetryConfig {
    @Bean
    public RetryTemplate authApiRetryTemplate(@Qualifier("authApiRetryListener") RetryListener retryListener) {
        var retryPolicy = RetryPolicy.builder()
                .maxRetries(1)
                .delay(Duration.ofMillis(100))
                .timeout(Duration.ofMillis(150))
                .includes(RetryableRemoteException.class)
                .build();
        var retryTemplate = new RetryTemplate(retryPolicy);
        retryTemplate.setRetryListener(retryListener);
        return retryTemplate;
    }
}
