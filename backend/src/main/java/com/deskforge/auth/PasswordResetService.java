package com.deskforge.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;

import com.deskforge.notification.EmailOutbox;
import com.deskforge.notification.EmailTemplate;
import com.deskforge.shared.exception.BusinessRuleException;
import com.deskforge.user.User;
import com.deskforge.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {

    private static final String INVALID_LINK = "Este link de redefinição é inválido ou expirou. Peça um novo.";
    private static final int TOKEN_BYTES = 32;

    private final PasswordResetTokenRepository tokens;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final EmailOutbox outbox;
    private final PasswordResetProperties properties;
    private final String frontendUrl;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public PasswordResetService(PasswordResetTokenRepository tokens, UserRepository users,
                                PasswordEncoder passwordEncoder, EmailOutbox outbox,
                                PasswordResetProperties properties,
                                @Value("${deskforge.frontend-url}") String frontendUrl, Clock clock) {
        this.tokens = tokens;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.outbox = outbox;
        this.properties = properties;
        this.frontendUrl = frontendUrl;
        this.clock = clock;
    }

    @Transactional
    public void requestReset(String email) {
        Optional<User> found = users.findByEmail(email.strip().toLowerCase()).filter(user -> !user.isAnonymized());
        if (found.isEmpty()) {
            return;
        }
        User user = found.get();
        Instant now = clock.instant();
        boolean recentlyRequested = tokens.findFirstByUserIdOrderByCreatedAtDesc(user.getId())
                .map(last -> last.getCreatedAt().plus(properties.requestCooldown()).isAfter(now))
                .orElse(false);
        if (recentlyRequested) {
            return;
        }

        tokens.invalidateOpenTokens(user.getId(), now);
        String rawToken = newToken();
        PasswordResetToken token = tokens.save(PasswordResetToken.issue(user, hash(rawToken), now, properties.validity()));
        outbox.enqueue("password-reset:" + token.getId(), EmailTemplate.PASSWORD_RESET, user.getEmail(),
                "Redefina sua senha da DeskForge",
                Map.of("firstName", user.getFirstName(),
                        "resetUrl", frontendUrl + "/redefinir-senha#token=" + rawToken,
                        "validityMinutes", properties.validity().toMinutes()));
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        Instant now = clock.instant();
        PasswordResetToken token = tokens.findByTokenHash(hash(rawToken))
                .filter(found -> found.isUsable(now))
                .orElseThrow(() -> new BusinessRuleException(INVALID_LINK));
        User user = token.getUser();
        if (user.isAnonymized()) {
            throw new BusinessRuleException(INVALID_LINK);
        }

        user.changePassword(passwordEncoder.encode(newPassword));
        token.markUsed(now);
        tokens.invalidateOpenTokens(user.getId(), now);
        outbox.enqueue("password-changed:" + token.getId(), EmailTemplate.PASSWORD_CHANGED, user.getEmail(),
                "Sua senha da DeskForge foi alterada",
                Map.of("firstName", user.getFirstName(),
                        "forgotUrl", frontendUrl + "/esqueci-senha"));
    }

    private String newToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 indisponível", ex);
        }
    }
}
