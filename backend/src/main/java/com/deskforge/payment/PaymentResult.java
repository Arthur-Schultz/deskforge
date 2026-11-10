package com.deskforge.payment;

import java.time.Instant;

public record PaymentResult(boolean approved, String reference, String redirectUrl, Instant expiresAt) {

    public static PaymentResult approved(String reference) {
        return new PaymentResult(true, reference, null, null);
    }

    public static PaymentResult redirect(String reference, String redirectUrl, Instant expiresAt) {
        return new PaymentResult(false, reference, redirectUrl, expiresAt);
    }
}
