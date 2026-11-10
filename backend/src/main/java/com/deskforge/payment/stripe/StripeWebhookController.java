package com.deskforge.payment.stripe;

import java.util.regex.Pattern;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);
    private static final Pattern WEBHOOK_SECRET = Pattern.compile("^whsec_[A-Za-z0-9]{24,}$");

    private final StripeWebhookService webhookService;
    private final String webhookSecret;

    public StripeWebhookController(StripeWebhookService webhookService, StripeProperties properties) {
        if (properties.webhookSecret() == null || !WEBHOOK_SECRET.matcher(properties.webhookSecret()).matches()) {
            throw new IllegalStateException("STRIPE_WEBHOOK_SECRET ausente ou inválido. Rode 'stripe listen' e copie "
                    + "o valor completo exibido em 'Your webhook signing secret is whsec_...' para o .env");
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
        } catch (SignatureVerificationException ex) {
            log.warn("Stripe webhook rejected: invalid signature. Check that STRIPE_WEBHOOK_SECRET matches the "
                    + "secret shown by 'stripe listen'");
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException ex) {
            log.warn("Stripe webhook rejected: malformed payload ({})", ex.getMessage());
            return ResponseEntity.badRequest().build();
        }
        webhookService.handle(event);
        return ResponseEntity.ok().build();
    }
}
