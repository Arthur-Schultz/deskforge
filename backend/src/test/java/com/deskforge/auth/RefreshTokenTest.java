package com.deskforge.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import com.deskforge.user.User;
import org.junit.jupiter.api.Test;

class RefreshTokenTest {

    private static final Instant NOW = Instant.parse("2026-11-11T12:00:00Z");

    @Test
    void isActiveUntilItExpiresOrIsRevoked() {
        RefreshToken token = token();

        assertThat(token.isActive(NOW.plus(Duration.ofDays(13)))).isTrue();
        assertThat(token.isActive(NOW.plus(Duration.ofDays(14)))).isFalse();

        token.revoke(RevokeReason.LOGOUT, NOW);
        assertThat(token.isActive(NOW)).isFalse();
    }

    @Test
    void aJustRotatedTokenIsToleratedForAShortGracePeriod() {
        RefreshToken token = token();
        token.revoke(RevokeReason.ROTATED, NOW);

        assertThat(token.wasRotatedWithin(Duration.ofSeconds(30), NOW.plusSeconds(10))).isTrue();
        assertThat(token.wasRotatedWithin(Duration.ofSeconds(30), NOW.plusSeconds(31))).isFalse();
    }

    @Test
    void theFirstRevocationReasonIsKept() {
        RefreshToken token = token();
        token.revoke(RevokeReason.ROTATED, NOW);
        token.revoke(RevokeReason.REUSE_DETECTED, NOW.plusSeconds(60));

        assertThat(token.getRevokeReason()).isEqualTo(RevokeReason.ROTATED);
        assertThat(token.getRevokedAt()).isEqualTo(NOW);
    }

    private static RefreshToken token() {
        User user = User.newCustomer("Teste", "t@test.dev", "hash", false, "v1", NOW);
        return RefreshToken.issue(user, UUID.randomUUID(), "0".repeat(64), NOW, Duration.ofDays(14));
    }
}
