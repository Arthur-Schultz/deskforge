package com.deskforge.privacy;

import com.deskforge.auth.AuthenticatedUser;
import com.deskforge.auth.CurrentUser;
import com.deskforge.auth.SessionCookies;
import com.deskforge.demo.DemoPolicy;
import com.deskforge.auth.dto.UserResponse;
import com.deskforge.privacy.dto.AccountDataExport;
import com.deskforge.privacy.dto.ConsentUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@Tag(name = "Minha conta")
public class AccountController {

    private final PrivacyService privacyService;
    private final SessionCookies sessionCookies;
    private final DemoPolicy demoPolicy;

    public AccountController(PrivacyService privacyService, SessionCookies sessionCookies, DemoPolicy demoPolicy) {
        this.privacyService = privacyService;
        this.sessionCookies = sessionCookies;
        this.demoPolicy = demoPolicy;
    }

    @GetMapping
    @Operation(summary = "Perfil do usuário autenticado")
    public UserResponse me() {
        return privacyService.profile(CurrentUser.get().id());
    }

    @PatchMapping("/consents")
    @Operation(summary = "Concede ou revoga o consentimento para comunicações de marketing")
    public UserResponse updateConsents(@RequestBody ConsentUpdateRequest request) {
        return privacyService.updateMarketingConsent(CurrentUser.get().id(), request.marketingConsent());
    }

    @GetMapping("/data-export")
    @Operation(summary = "Exporta todos os dados pessoais do titular (portabilidade)")
    public ResponseEntity<AccountDataExport> export() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"deskforge-meus-dados.json\"")
                .body(privacyService.export(CurrentUser.get().id()));
    }

    @DeleteMapping
    @Operation(summary = "Exclui a conta anonimizando os dados pessoais e encerra todas as sessões")
    public ResponseEntity<Void> delete() {
        AuthenticatedUser user = CurrentUser.get();
        demoPolicy.ensureAccountCanChange(user.email());
        privacyService.anonymize(user.id());
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, sessionCookies.clear().toString()).build();
    }
}
