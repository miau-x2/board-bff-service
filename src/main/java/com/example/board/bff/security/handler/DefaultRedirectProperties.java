package com.example.board.bff.security.handler;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.redirect")
public record DefaultRedirectProperties(String defaultTargetUrl) {
}
