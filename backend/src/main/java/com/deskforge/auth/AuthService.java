package com.deskforge.auth;

import java.time.Clock;

import com.deskforge.auth.dto.AuthResponse;
import com.deskforge.auth.dto.LoginRequest;
import com.deskforge.auth.dto.RegisterRequest;
import com.deskforge.auth.dto.UserResponse;
import com.deskforge.shared.exception.ConflictException;
import com.deskforge.store.StoreProperties;
import com.deskforge.user.User;
import com.deskforge.user.UserRepository;
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
    private final StoreProperties store;
    private final Clock clock;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService,
                       StoreProperties store, Clock clock) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.store = store;
        this.clock = clock;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().strip().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new ConflictException("Já existe uma conta com este e-mail");
        }
        User user = User.newCustomer(request.fullName(), email, passwordEncoder.encode(request.password()),
                request.marketingConsent(), store.privacyPolicyVersion(), clock.instant());
        users.save(user);
        return tokenFor(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = users.findByEmail(request.email().strip().toLowerCase())
                .filter(found -> !found.isAnonymized())
                .orElseThrow(() -> new BadCredentialsException(INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException(INVALID_CREDENTIALS);
        }
        return tokenFor(user);
    }

    private AuthResponse tokenFor(User user) {
        JwtService.IssuedToken token = jwtService.issue(user);
        return new AuthResponse(token.value(), "Bearer", token.expiresAt(), UserResponse.from(user));
    }
}
