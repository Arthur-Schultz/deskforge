package com.deskforge.auth;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deskforge.security.jwt")
public record JwtProperties(String secret, Duration expiration, String issuer) {
}
