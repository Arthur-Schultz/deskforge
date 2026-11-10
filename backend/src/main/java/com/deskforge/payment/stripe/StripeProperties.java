package com.deskforge.payment.stripe;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deskforge.payment.stripe")
public record StripeProperties(String secretKey, String webhookSecret) {
}
