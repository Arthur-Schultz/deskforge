package com.deskforge.shared.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RateLimiter {

    private static final int MAX_TRACKED_KEYS = 100_000;

    private final ConcurrentMap<String, Window> windows = new ConcurrentHashMap<>();
    private final Clock clock;

    public RateLimiter(Clock clock) {
        this.clock = clock;
    }

    public void check(String key, RateLimit limit) {
        Instant now = clock.instant();
        Window window = windows.get(key);
        if (window != null && window.isActive(now) && window.count() >= limit.maxAttempts()) {
            throw new TooManyRequestsException(Duration.between(now, window.endsAt()));
        }
    }

    public void record(String key, RateLimit limit) {
        Instant now = clock.instant();
        if (windows.size() >= MAX_TRACKED_KEYS) {
            evictExpired();
        }
        windows.compute(key, (ignored, current) -> current == null || !current.isActive(now)
                ? new Window(now.plus(limit.window()), 1)
                : new Window(current.endsAt(), current.count() + 1));
    }

    public void consume(String key, RateLimit limit) {
        check(key, limit);
        record(key, limit);
    }

    public void reset(String key) {
        windows.remove(key);
    }

    @Scheduled(fixedDelay = 600_000)
    public void evictExpired() {
        Instant now = clock.instant();
        windows.entrySet().removeIf(entry -> !entry.getValue().isActive(now));
    }

    private record Window(Instant endsAt, int count) {

        boolean isActive(Instant now) {
            return now.isBefore(endsAt);
        }
    }
}
