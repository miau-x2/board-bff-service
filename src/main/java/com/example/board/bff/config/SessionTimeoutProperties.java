package com.example.board.bff.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.session")
public record SessionTimeoutProperties(Duration absoluteTimeout) {
}
