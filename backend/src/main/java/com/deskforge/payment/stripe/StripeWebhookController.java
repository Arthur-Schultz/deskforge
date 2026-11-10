package com.deskforge.payment.stripe;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@ConditionalOnProperty(name = "deskforge.payment.provider", havingValue = "stripe")
public class StripeWebhookController {

    private final StripeWebhookService webhookService;
    private final String webhookSecret;

    public StripeWebhookController(StripeWebhookService webhookService, StripeProperties properties) {
        if (properties.webhookSecret() == null || !properties.webhookSecret().startsWith("whsec_")) {
            throw new IllegalStateException(
                    "STRIPE_WEBHOOK_SECRET não definido. Rode 'stripe listen' e copie o valor whsec_ para o .env");
        }
        this.webhookService = webhookService;
        this.webhookSecret = properties.webhookSecret();
    }

    @PostMapping("/api/payments/stripe/webhook")
    public ResponseEntity<Void> receive(@RequestBody String payload,
                                        @RequestHeader(name = "Stripe-Signature", required = false) String signature) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (SignatureVerificationException | RuntimeException ex) {
            return ResponseEntity.badRequest().build();
        }
        webhookService.handle(event);
        return ResponseEntity.ok().build();
    }
}
