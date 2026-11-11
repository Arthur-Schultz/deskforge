package com.deskforge.auth;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import com.deskforge.demo.DemoPolicy;
import com.deskforge.notification.EmailOutbox;
import com.deskforge.notification.EmailTemplate;
import com.deskforge.shared.exception.BusinessRuleException;
import com.deskforge.shared.ratelimit.RateLimiter;
import com.deskforge.shared.security.SecureTokens;
import com.deskforge.user.User;
import com.deskforge.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {

    private static final String INVALID_LINK = "Este link de redefinição é inválido ou expirou. Peça um novo.";

    private final PasswordResetTokenRepository tokens;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final EmailOutbox outbox;
    private final PasswordResetProperties properties;
    private final SessionService sessions;
    private final RateLimiter rateLimiter;
    private final AuthRateLimits limits;
    private final DemoPolicy demoPolicy;
    private final String frontendUrl;
    private final Clock clock;

    public PasswordResetService(PasswordResetTokenRepository tokens, UserRepository users,
                                PasswordEncoder passwordEncoder, EmailOutbox outbox,
                                PasswordResetProperties properties, SessionService sessions,
                                RateLimiter rateLimiter, AuthRateLimits limits, DemoPolicy demoPolicy,
                                @Value("${deskforge.frontend-url}") String frontendUrl, Clock clock) {
        this.tokens = tokens;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.outbox = outbox;
        this.properties = properties;
        this.sessions = sessions;
        this.rateLimiter = rateLimiter;
        this.limits = limits;
        this.demoPolicy = demoPolicy;
        this.frontendUrl = frontendUrl;
        this.clock = clock;
    }

    @Transactional
    public void requestReset(String email, String clientIp) {
        rateLimiter.consume("forgot-password:ip:" + clientIp, limits.forgotPasswordPerIp());
        Optional<User> found = users.findByEmail(email.strip().toLowerCase()).filter(user -> !user.isAnonymized());
        if (found.isEmpty()) {
            return;
        }
        User user = found.get();
        if (demoPolicy.isProtectedAccount(user.getEmail())) {
            return;
        }
        Instant now = clock.instant();
        boolean recentlyRequested = tokens.findFirstByUserIdOrderByCreatedAtDesc(user.getId())
                .map(last -> last.getCreatedAt().plus(properties.requestCooldown()).isAfter(now))
                .orElse(false);
        if (recentlyRequested) {
            return;
        }

        tokens.invalidateOpenTokens(user.getId(), now);
        String rawToken = SecureTokens.newToken();
        PasswordResetToken token = tokens.save(
                PasswordResetToken.issue(user, SecureTokens.sha256(rawToken), now, properties.validity()));
        outbox.enqueue("password-reset:" + token.getId(), EmailTemplate.PASSWORD_RESET, user.getEmail(),
                "Redefina sua senha da DeskForge",
                Map.of("firstName", user.getFirstName(),
                        "resetUrl", frontendUrl + "/redefinir-senha#token=" + rawToken,
                        "validityMinutes", properties.validity().toMinutes()));
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword, String clientIp) {
        rateLimiter.consume("reset-password:ip:" + clientIp, limits.resetPasswordPerIp());
        Instant now = clock.instant();
        PasswordResetToken token = tokens.findByTokenHash(SecureTokens.sha256(rawToken))
                .filter(found -> found.isUsable(now))
                .orElseThrow(() -> new BusinessRuleException(INVALID_LINK));
        User user = token.getUser();
        if (user.isAnonymized() || demoPolicy.isProtectedAccount(user.getEmail())) {
            throw new BusinessRuleException(INVALID_LINK);
        }

        user.changePassword(passwordEncoder.encode(newPassword));
        token.markUsed(now);
        tokens.invalidateOpenTokens(user.getId(), now);
        sessions.endAll(user.getId(), RevokeReason.PASSWORD_CHANGED);
        outbox.enqueue("password-changed:" + token.getId(), EmailTemplate.PASSWORD_CHANGED, user.getEmail(),
                "Sua senha da DeskForge foi alterada",
                Map.of("firstName", user.getFirstName(),
                        "forgotUrl", frontendUrl + "/esqueci-senha"));
    }
}
