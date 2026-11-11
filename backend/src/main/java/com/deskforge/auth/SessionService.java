package com.deskforge.auth;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import com.deskforge.shared.security.SecureTokens;
import com.deskforge.user.User;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    private final RefreshTokenRepository tokens;
    private final SessionProperties properties;
    private final Clock clock;
    private final MeterRegistry registry;

    public SessionService(RefreshTokenRepository tokens, SessionProperties properties, Clock clock,
                          MeterRegistry registry) {
        this.tokens = tokens;
        this.properties = properties;
        this.clock = clock;
        this.registry = registry;
        registry.counter("deskforge.auth.refresh.reuse");
    }

    @Transactional
    public String start(User user) {
        return issue(user, UUID.randomUUID(), clock.instant());
    }

    @Transactional(noRollbackFor = InvalidSessionException.class)
    public Rotated rotate(String rawToken) {
        Instant now = clock.instant();
        RefreshToken current = tokens.findForUpdateByTokenHash(SecureTokens.sha256(rawToken))
                .orElseThrow(InvalidSessionException::new);
        if (!current.isActive(now)) {
            if (current.getRevokedAt() != null && !current.wasRotatedWithin(properties.rotationGrace(), now)) {
                int revoked = tokens.revokeFamily(current.getFamilyId(), RevokeReason.REUSE_DETECTED, now);
                if (current.getRevokeReason() == RevokeReason.ROTATED) {
                    registry.counter("deskforge.auth.refresh.reuse").increment();
                    log.warn("Refresh token reuse detected for user {}, revoked {} sessions",
                            current.getUser().getId(), revoked);
                }
            }
            throw new InvalidSessionException();
        }
        User user = current.getUser();
        if (user.isAnonymized()) {
            current.revoke(RevokeReason.ACCOUNT_DELETED, now);
            throw new InvalidSessionException();
        }
        current.revoke(RevokeReason.ROTATED, now);
        return new Rotated(user, issue(user, current.getFamilyId(), now));
    }

    @Transactional
    public void end(String rawToken) {
        Instant now = clock.instant();
        tokens.findForUpdateByTokenHash(SecureTokens.sha256(rawToken))
                .ifPresent(token -> tokens.revokeFamily(token.getFamilyId(), RevokeReason.LOGOUT, now));
    }

    @Transactional
    public int endAll(UUID userId, RevokeReason reason) {
        return tokens.revokeAllForUser(userId, reason, clock.instant());
    }

    @Transactional
    public int purgeExpired() {
        return tokens.deleteExpiredBefore(clock.instant());
    }

    private String issue(User user, UUID familyId, Instant now) {
        String raw = SecureTokens.newToken();
        tokens.save(RefreshToken.issue(user, familyId, SecureTokens.sha256(raw), now,
                properties.refreshTokenValidity()));
        return raw;
    }

    public record Rotated(User user, String refreshToken) {
    }
}
