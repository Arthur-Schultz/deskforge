package com.deskforge.auth;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deskforge.security.session")
public record SessionProperties(
        Duration refreshTokenValidity,
        Duration rotationGrace,
        String cookieName,
        boolean cookieSecure) {
}
