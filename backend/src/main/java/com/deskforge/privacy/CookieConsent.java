package com.deskforge.privacy;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cookie_consents")
public class CookieConsent {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String visitorId;

    @Column(nullable = false)
    private boolean analytics;

    @Column(nullable = false)
    private boolean marketing;

    @Column(nullable = false)
    private String policyVersion;

    @Column(nullable = false)
    private Instant createdAt;

    protected CookieConsent() {
    }

    public CookieConsent(String visitorId, boolean analytics, boolean marketing, String policyVersion, Instant now) {
        this.id = UUID.randomUUID();
        this.visitorId = visitorId;
        this.analytics = analytics;
        this.marketing = marketing;
        this.policyVersion = policyVersion;
        this.createdAt = now;
    }

    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
