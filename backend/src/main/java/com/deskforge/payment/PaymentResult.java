package com.deskforge.payment;

public record PaymentResult(boolean approved, String reference, String redirectUrl) {
}
