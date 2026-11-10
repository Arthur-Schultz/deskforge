package com.deskforge.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import com.deskforge.user.Role;
import com.deskforge.user.User;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "dGVzdC1zZWNyZXQtdGhhdC1pcy1sb25nLWVub3VnaC1mb3ItaHMyNTYtc2lnbmluZw==";
    private static final Instant NOW = Instant.parse("2026-11-10T12:00:00Z");

    private final JwtProperties properties = new JwtProperties(SECRET, Duration.ofHours(1), "deskforge-test");
    private final User user = User.newCustomer("Cliente", "cliente@test.dev", "hash", false, "v1", NOW);

    @Test
    void issuesAndParsesTokens() {
        JwtService service = new JwtService(properties, Clock.fixed(NOW, ZoneOffset.UTC));

        String token = service.issue(user).value();

        assertThat(service.parse(token)).hasValueSatisfying(parsed -> {
            assertThat(parsed.id()).isEqualTo(user.getId());
            assertThat(parsed.email()).isEqualTo("cliente@test.dev");
            assertThat(parsed.role()).isEqualTo(Role.CUSTOMER);
        });
    }

    @Test
    void rejectsExpiredTokens() {
        String token = new JwtService(properties, Clock.fixed(NOW, ZoneOffset.UTC)).issue(user).value();
        JwtService later = new JwtService(properties, Clock.fixed(NOW.plus(Duration.ofHours(2)), ZoneOffset.UTC));

        assertThat(later.parse(token)).isEmpty();
    }

    @Test
    void rejectsTamperedTokens() {
        JwtService service = new JwtService(properties, Clock.fixed(NOW, ZoneOffset.UTC));
        String token = service.issue(user).value();

        assertThat(service.parse(token.substring(0, token.length() - 2) + "xx")).isEmpty();
    }
}
