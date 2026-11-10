package com.deskforge.support;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "contact_messages")
public class ContactMessage {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String protocol;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String message;

    private String orderNumber;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant respondBy;

    protected ContactMessage() {
    }

    public ContactMessage(String protocol, String fullName, String email, String subject, String message,
                          String orderNumber, Instant now, Duration responseTime) {
        this.id = UUID.randomUUID();
        this.protocol = protocol;
        this.fullName = fullName;
        this.email = email;
        this.subject = subject;
        this.message = message;
        this.orderNumber = orderNumber;
        this.createdAt = now;
        this.respondBy = now.plus(responseTime);
    }

    public String getProtocol() {
        return protocol;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getRespondBy() {
        return respondBy;
    }
}
