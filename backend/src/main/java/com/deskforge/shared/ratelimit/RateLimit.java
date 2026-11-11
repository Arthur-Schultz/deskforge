package com.deskforge.shared.ratelimit;

import java.time.Duration;

public record RateLimit(int maxAttempts, Duration window) {
}
