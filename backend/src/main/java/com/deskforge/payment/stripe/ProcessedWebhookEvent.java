package com.deskforge.payment.stripe;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "processed_webhook_events")
public class ProcessedWebhookEvent {

    @Id
    private String eventId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private Instant processedAt;

    protected ProcessedWebhookEvent() {
    }

    public ProcessedWebhookEvent(String eventId, String type, Instant processedAt) {
        this.eventId = eventId;
        this.type = type;
        this.processedAt = processedAt;
    }
}
