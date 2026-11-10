package com.deskforge.audit;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "access_logs")
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private String method;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private int status;

    private UUID userId;

    @Column(nullable = false)
    private Instant createdAt;

    protected AccessLog() {
    }

    public AccessLog(String ipAddress, String method, String path, int status, UUID userId, Instant createdAt) {
        this.ipAddress = ipAddress;
        this.method = method;
        this.path = path;
        this.status = status;
        this.userId = userId;
        this.createdAt = createdAt;
    }
}
