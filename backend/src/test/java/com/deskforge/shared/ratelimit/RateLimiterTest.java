package com.deskforge.shared.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class RateLimiterTest {

    private static final RateLimit THREE_PER_MINUTE = new RateLimit(3, Duration.ofMinutes(1));

    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-11T12:00:00Z"));
    private final RateLimiter limiter = new RateLimiter(clock);

    @Test
    void blocksAfterTheLimitAndTellsWhenToRetry() {
        for (int i = 0; i < 3; i++) {
            limiter.consume("k", THREE_PER_MINUTE);
        }
        clock.advance(Duration.ofSeconds(20));

        assertThatThrownBy(() -> limiter.consume("k", THREE_PER_MINUTE))
                .isInstanceOfSatisfying(TooManyRequestsException.class, ex -> {
                    assertThat(ex.getRetryAfter()).isEqualTo(Duration.ofSeconds(40));
                    assertThat(ex.getMessage()).isEqualTo("Muitas tentativas. Tente de novo em 1 minuto.");
                });
    }

    @Test
    void theWindowResetsAfterItEnds() {
        for (int i = 0; i < 3; i++) {
            limiter.consume("k", THREE_PER_MINUTE);
        }
        clock.advance(Duration.ofMinutes(1));

        limiter.consume("k", THREE_PER_MINUTE);
    }

    @Test
    void keysAreIndependentAndCanBeReset() {
        for (int i = 0; i < 3; i++) {
            limiter.record("a", THREE_PER_MINUTE);
        }
        limiter.check("b", THREE_PER_MINUTE);
        limiter.reset("a");
        limiter.check("a", THREE_PER_MINUTE);
    }

    private static final class MutableClock extends Clock {

        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
