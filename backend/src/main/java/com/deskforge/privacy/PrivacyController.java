package com.deskforge.privacy;

import com.deskforge.privacy.dto.CookieConsentRequest;
import com.deskforge.privacy.dto.CookieConsentResponse;
import com.deskforge.privacy.dto.DataSubjectRequestPayload;
import com.deskforge.privacy.dto.ProtocolResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/privacy")
@Tag(name = "Privacidade (LGPD)")
public class PrivacyController {

    private final PrivacyService privacyService;

    public PrivacyController(PrivacyService privacyService) {
        this.privacyService = privacyService;
    }

    @PostMapping("/consents")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra a escolha de cookies do visitante como prova de consentimento")
    public CookieConsentResponse consent(@Valid @RequestBody CookieConsentRequest request) {
        return privacyService.recordConsent(request);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Abre uma solicitação de direitos do titular (art. 18 da LGPD)")
    public ProtocolResponse request(@Valid @RequestBody DataSubjectRequestPayload payload) {
        return privacyService.openRequest(payload);
    }
}
