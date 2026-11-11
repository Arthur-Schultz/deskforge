package com.deskforge.auth;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deskforge.security.password-reset")
public record PasswordResetProperties(Duration validity, Duration requestCooldown) {
}
