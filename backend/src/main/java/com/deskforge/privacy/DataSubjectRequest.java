package com.deskforge.privacy;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "data_subject_requests")
public class DataSubjectRequest {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String protocol;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataSubjectRequestType type;

    private String details;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant dueAt;

    protected DataSubjectRequest() {
    }

    public DataSubjectRequest(String protocol, String fullName, String email, DataSubjectRequestType type,
                              String details, Instant now, Duration responseTime) {
        this.id = UUID.randomUUID();
        this.protocol = protocol;
        this.fullName = fullName;
        this.email = email;
        this.type = type;
        this.details = details;
        this.status = "OPEN";
        this.createdAt = now;
        this.dueAt = now.plus(responseTime);
    }

    public String getProtocol() {
        return protocol;
    }

    public DataSubjectRequestType getType() {
        return type;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getDueAt() {
        return dueAt;
    }
}
