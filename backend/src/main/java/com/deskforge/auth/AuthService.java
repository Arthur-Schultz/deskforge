package com.deskforge.auth;

import java.time.Clock;
import java.util.UUID;

import com.deskforge.auth.dto.AuthResponse;
import com.deskforge.auth.dto.LoginRequest;
import com.deskforge.auth.dto.RegisterRequest;
import com.deskforge.auth.dto.UserResponse;
import com.deskforge.demo.DemoPolicy;
import com.deskforge.shared.exception.ConflictException;
import com.deskforge.shared.ratelimit.RateLimiter;
import com.deskforge.shared.ratelimit.TooManyRequestsException;
import com.deskforge.store.StoreProperties;
import com.deskforge.user.User;
import com.deskforge.user.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String INVALID_CREDENTIALS = "E-mail ou senha inválidos";

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SessionService sessions;
    private final RateLimiter rateLimiter;
    private final AuthRateLimits limits;
    private final DemoPolicy demoPolicy;
    private final StoreProperties store;
    private final Clock clock;
    private final MeterRegistry registry;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService,
                       SessionService sessions, RateLimiter rateLimiter, AuthRateLimits limits,
                       DemoPolicy demoPolicy, StoreProperties store, Clock clock, MeterRegistry registry) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.sessions = sessions;
        this.rateLimiter = rateLimiter;
        this.limits = limits;
        this.demoPolicy = demoPolicy;
        this.store = store;
        this.clock = clock;
        this.registry = registry;
        for (String outcome : new String[] {"success", "invalid", "blocked"}) {
            registry.counter("deskforge.auth.logins", "outcome", outcome);
        }
    }

    @Transactional
    public SignedIn register(RegisterRequest request, String clientIp) {
        rateLimiter.consume("register:ip:" + clientIp, limits.registerPerIp());
        String email = request.email().strip().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new ConflictException("Já existe uma conta com este e-mail");
        }
        User user = User.newCustomer(request.fullName(), email, passwordEncoder.encode(request.password()),
                request.marketingConsent(), store.privacyPolicyVersion(), clock.instant());
        users.save(user);
        return signIn(user, sessions.start(user));
    }

    @Transactional
    public SignedIn login(LoginRequest request, String clientIp) {
        String email = request.email().strip().toLowerCase();
        String accountKey = "login:account:" + email;
        String ipKey = "login:ip:" + clientIp;
        try {
            rateLimiter.check(accountKey, limits.loginPerAccount());
            rateLimiter.check(ipKey, limits.loginPerIp());
        } catch (TooManyRequestsException ex) {
            registry.counter("deskforge.auth.logins", "outcome", "blocked").increment();
            throw ex;
        }

        User user = users.findByEmail(email).filter(found -> !found.isAnonymized()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            rateLimiter.record(accountKey, limits.loginPerAccount());
            rateLimiter.record(ipKey, limits.loginPerIp());
            registry.counter("deskforge.auth.logins", "outcome", "invalid").increment();
            throw new BadCredentialsException(INVALID_CREDENTIALS);
        }
        rateLimiter.reset(accountKey);
        registry.counter("deskforge.auth.logins", "outcome", "success").increment();
        return signIn(user, sessions.start(user));
    }

    @Transactional(noRollbackFor = InvalidSessionException.class)
    public SignedIn refresh(String refreshToken) {
        SessionService.Rotated rotated = sessions.rotate(refreshToken);
        return signIn(rotated.user(), rotated.refreshToken());
    }

    @Transactional
    public void logout(String refreshToken) {
        sessions.end(refreshToken);
    }

    @Transactional
    public void logoutEverywhere(UUID userId, String email) {
        demoPolicy.ensureAccountCanChange(email);
        sessions.endAll(userId, RevokeReason.LOGOUT_ALL);
    }

    private SignedIn signIn(User user, String refreshToken) {
        JwtService.IssuedToken token = jwtService.issue(user);
        AuthResponse body = new AuthResponse(token.value(), "Bearer", token.expiresAt(), UserResponse.from(user));
        return new SignedIn(body, refreshToken);
    }

    public record SignedIn(AuthResponse body, String refreshToken) {
    }
}
