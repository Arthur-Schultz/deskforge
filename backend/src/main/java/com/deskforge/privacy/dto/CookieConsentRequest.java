package com.deskforge.privacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CookieConsentRequest(
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9-]{8,64}$") String visitorId,
        boolean analytics,
        boolean marketing) {
}
