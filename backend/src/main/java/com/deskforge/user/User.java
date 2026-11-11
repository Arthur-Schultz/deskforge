package com.deskforge.user;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean marketingConsent;

    @Column(nullable = false)
    private Instant termsAcceptedAt;

    @Column(nullable = false)
    private String privacyPolicyVersion;

    private Instant anonymizedAt;

    @Column(nullable = false)
    private Instant createdAt;

    protected User() {
    }

    public static User newCustomer(String fullName, String email, String passwordHash, boolean marketingConsent,
                                   String privacyPolicyVersion, Instant now) {
        User user = new User();
        user.id = UUID.randomUUID();
        user.fullName = fullName.strip();
        user.email = email.strip().toLowerCase();
        user.passwordHash = passwordHash;
        user.role = Role.CUSTOMER;
        user.marketingConsent = marketingConsent;
        user.termsAcceptedAt = now;
        user.privacyPolicyVersion = privacyPolicyVersion;
        user.createdAt = now;
        return user;
    }

    public void anonymize(String unusablePasswordHash, Instant now) {
        this.fullName = "Titular removido";
        this.email = "removed-" + id + "@anonymized.invalid";
        this.passwordHash = unusablePasswordHash;
        this.marketingConsent = false;
        this.anonymizedAt = now;
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    public void updateMarketingConsent(boolean marketingConsent) {
        this.marketingConsent = marketingConsent;
    }

    public boolean isAnonymized() {
        return anonymizedAt != null;
    }

    public UUID getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getFirstName() {
        int space = fullName.indexOf(' ');
        return space < 0 ? fullName : fullName.substring(0, space);
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public boolean isMarketingConsent() {
        return marketingConsent;
    }

    public Instant getTermsAcceptedAt() {
        return termsAcceptedAt;
    }

    public String getPrivacyPolicyVersion() {
        return privacyPolicyVersion;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
