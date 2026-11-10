package com.deskforge.privacy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import com.deskforge.auth.dto.UserResponse;
import com.deskforge.order.OrderService;
import com.deskforge.privacy.dto.AccountDataExport;
import com.deskforge.privacy.dto.CookieConsentRequest;
import com.deskforge.privacy.dto.CookieConsentResponse;
import com.deskforge.privacy.dto.DataSubjectRequestPayload;
import com.deskforge.privacy.dto.ProtocolResponse;
import com.deskforge.shared.exception.NotFoundException;
import com.deskforge.shared.web.ProtocolGenerator;
import com.deskforge.store.StoreProperties;
import com.deskforge.user.User;
import com.deskforge.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrivacyService {

    private static final String RETENTION_NOTE = "Pedidos são mantidos após a exclusão da conta para cumprimento de "
            + "obrigação legal e fiscal (art. 16, I, da LGPD), desvinculados de dados de contato.";

    private final CookieConsentRepository consents;
    private final DataSubjectRequestRepository requests;
    private final UserRepository users;
    private final OrderService orderService;
    private final PasswordEncoder passwordEncoder;
    private final StoreProperties store;
    private final Clock clock;

    public PrivacyService(CookieConsentRepository consents, DataSubjectRequestRepository requests,
                          UserRepository users, OrderService orderService, PasswordEncoder passwordEncoder,
                          StoreProperties store, Clock clock) {
        this.consents = consents;
        this.requests = requests;
        this.users = users;
        this.orderService = orderService;
        this.passwordEncoder = passwordEncoder;
        this.store = store;
        this.clock = clock;
    }

    @Transactional
    public CookieConsentResponse recordConsent(CookieConsentRequest request) {
        CookieConsent consent = consents.save(new CookieConsent(request.visitorId(), request.analytics(),
                request.marketing(), store.privacyPolicyVersion(), clock.instant()));
        return new CookieConsentResponse(consent.getId(), store.privacyPolicyVersion(), consent.getCreatedAt());
    }

    @Transactional
    public ProtocolResponse openRequest(DataSubjectRequestPayload payload) {
        Instant now = clock.instant();
        DataSubjectRequest request = requests.save(new DataSubjectRequest(
                ProtocolGenerator.generate("LGPD", clock), payload.fullName().strip(),
                payload.email().strip().toLowerCase(), payload.type(), payload.details(), now,
                Duration.ofDays(store.dataSubjectResponseDays())));
        return new ProtocolResponse(request.getProtocol(), request.getCreatedAt(), request.getDueAt());
    }

    @Transactional(readOnly = true)
    public UserResponse profile(UUID userId) {
        return UserResponse.from(user(userId));
    }

    @Transactional
    public UserResponse updateMarketingConsent(UUID userId, boolean marketingConsent) {
        User user = user(userId);
        user.updateMarketingConsent(marketingConsent);
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public AccountDataExport export(UUID userId) {
        User user = user(userId);
        return new AccountDataExport(clock.instant(), store.legalName(), RETENTION_NOTE, UserResponse.from(user),
                user.getTermsAcceptedAt(), user.getPrivacyPolicyVersion(), orderService.listForCustomer(userId));
    }

    @Transactional
    public void anonymize(UUID userId) {
        User user = user(userId);
        user.anonymize(passwordEncoder.encode(UUID.randomUUID().toString()), clock.instant());
    }

    private User user(UUID userId) {
        return users.findById(userId)
                .filter(user -> !user.isAnonymized())
                .orElseThrow(() -> new NotFoundException("Conta não encontrada"));
    }
}
