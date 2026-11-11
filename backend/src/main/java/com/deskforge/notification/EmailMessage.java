package com.deskforge.notification;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "email_outbox")
public class EmailMessage {

    static final List<Duration> RETRY_DELAYS = List.of(
            Duration.ofMinutes(1), Duration.ofMinutes(5), Duration.ofMinutes(15), Duration.ofHours(1));
    static final int MAX_ATTEMPTS = RETRY_DELAYS.size() + 1;
    private static final int MAX_ERROR_LENGTH = 500;

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String dedupKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailTemplate template;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private String subject;

    private String htmlBody;

    private String textBody;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailStatus status;

    @Column(nullable = false)
    private int attempts;

    @Column(nullable = false)
    private Instant nextAttemptAt;

    private String lastError;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant sentAt;

    protected EmailMessage() {
    }

    public static EmailMessage queue(String dedupKey, EmailTemplate template, String recipient,
                                     RenderedEmail content, Instant now) {
        EmailMessage message = new EmailMessage();
        message.id = UUID.randomUUID();
        message.dedupKey = dedupKey;
        message.template = template;
        message.recipient = recipient;
        message.subject = content.subject();
        message.htmlBody = content.html();
        message.textBody = content.text();
        message.status = EmailStatus.PENDING;
        message.nextAttemptAt = now;
        message.createdAt = now;
        return message;
    }

    public void markSent(Instant now) {
        this.attempts++;
        this.status = EmailStatus.SENT;
        this.sentAt = now;
        this.lastError = null;
        this.htmlBody = null;
        this.textBody = null;
    }

    public void registerFailure(String error, Instant now) {
        this.attempts++;
        this.lastError = truncate(error);
        if (attempts >= MAX_ATTEMPTS) {
            this.status = EmailStatus.FAILED;
        } else {
            this.nextAttemptAt = now.plus(RETRY_DELAYS.get(attempts - 1));
        }
    }

    private static String truncate(String error) {
        if (error == null) {
            return "Erro desconhecido";
        }
        return error.length() <= MAX_ERROR_LENGTH ? error : error.substring(0, MAX_ERROR_LENGTH);
    }

    public UUID getId() {
        return id;
    }

    public String getDedupKey() {
        return dedupKey;
    }

    public EmailTemplate getTemplate() {
        return template;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getHtmlBody() {
        return htmlBody;
    }

    public String getTextBody() {
        return textBody;
    }

    public EmailStatus getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    public String getLastError() {
        return lastError;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }
}
