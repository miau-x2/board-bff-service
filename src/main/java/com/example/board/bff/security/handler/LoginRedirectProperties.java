package com.example.board.bff.security.handler;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.redirect.login")
public record LoginRedirectProperties(List<String> allowedOrigins) {
}
