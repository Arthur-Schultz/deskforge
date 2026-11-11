package com.deskforge.auth;

import com.deskforge.shared.ratelimit.RateLimit;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deskforge.security.rate-limits")
public record AuthRateLimits(
        RateLimit loginPerAccount,
        RateLimit loginPerIp,
        RateLimit registerPerIp,
        RateLimit forgotPasswordPerIp,
        RateLimit resetPasswordPerIp) {
}
