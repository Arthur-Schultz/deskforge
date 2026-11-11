package com.deskforge.shared.ratelimit;

import java.time.Duration;

public class TooManyRequestsException extends RuntimeException {

    private final Duration retryAfter;

    public TooManyRequestsException(Duration retryAfter) {
        super(message(retryAfter));
        this.retryAfter = retryAfter;
    }

    public Duration getRetryAfter() {
        return retryAfter;
    }

    private static String message(Duration retryAfter) {
        long minutes = Math.max(1, (retryAfter.toSeconds() + 59) / 60);
        return "Muitas tentativas. Tente de novo em " + minutes + (minutes == 1 ? " minuto." : " minutos.");
    }
}
