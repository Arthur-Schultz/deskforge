package com.deskforge.privacy.dto;

import java.time.Instant;
import java.util.UUID;

public record CookieConsentResponse(UUID receiptId, String policyVersion, Instant recordedAt) {
}
