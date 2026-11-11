package com.deskforge.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

class EmailMessageTest {

    private static final Instant NOW = Instant.parse("2026-11-11T12:00:00Z");

    @Test
    void sendingClearsTheContentAndKeepsTheAuditTrail() {
        EmailMessage message = queued();

        message.markSent(NOW);

        assertThat(message.getStatus()).isEqualTo(EmailStatus.SENT);
        assertThat(message.getSentAt()).isEqualTo(NOW);
        assertThat(message.getAttempts()).isEqualTo(1);
        assertThat(message.getHtmlBody()).isNull();
        assertThat(message.getTextBody()).isNull();
        assertThat(message.getSubject()).isEqualTo("Assunto");
    }

    @Test
    void failuresAreRetriedWithIncreasingDelays() {
        EmailMessage message = queued();

        message.registerFailure("Connection refused", NOW);
        assertThat(message.getNextAttemptAt()).isEqualTo(NOW.plus(Duration.ofMinutes(1)));

        message.registerFailure("Connection refused", NOW);
        assertThat(message.getNextAttemptAt()).isEqualTo(NOW.plus(Duration.ofMinutes(5)));
        assertThat(message.getStatus()).isEqualTo(EmailStatus.PENDING);
        assertThat(message.getLastError()).isEqualTo("Connection refused");
    }

    @Test
    void givesUpAfterTheLastAttemptButKeepsTheContentForManualReview() {
        EmailMessage message = queued();

        for (int attempt = 0; attempt < EmailMessage.MAX_ATTEMPTS; attempt++) {
            message.registerFailure("x".repeat(800), NOW);
        }

        assertThat(message.getStatus()).isEqualTo(EmailStatus.FAILED);
        assertThat(message.getAttempts()).isEqualTo(EmailMessage.MAX_ATTEMPTS);
        assertThat(message.getLastError()).hasSize(500);
        assertThat(message.getHtmlBody()).isNotNull();
    }

    private static EmailMessage queued() {
        return EmailMessage.queue("test:" + NOW, EmailTemplate.PASSWORD_CHANGED, "cliente@test.dev",
                new RenderedEmail("Assunto", "<p>Oi</p>", "Oi"), NOW);
    }
}
