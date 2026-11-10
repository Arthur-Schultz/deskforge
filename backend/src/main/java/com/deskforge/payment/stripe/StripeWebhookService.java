package com.deskforge.payment.stripe;

import java.time.Clock;
import java.util.UUID;

import com.deskforge.order.OrderService;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "deskforge.payment.provider", havingValue = "stripe")
public class StripeWebhookService {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookService.class);

    private final ProcessedWebhookEventRepository processedEvents;
    private final OrderService orderService;
    private final Clock clock;

    public StripeWebhookService(ProcessedWebhookEventRepository processedEvents, OrderService orderService,
                                Clock clock) {
        this.processedEvents = processedEvents;
        this.orderService = orderService;
        this.clock = clock;
    }

    @Transactional
    public void handle(Event event) {
        if (processedEvents.existsById(event.getId())) {
            log.info("Stripe event {} already processed, skipping", event.getId());
            return;
        }
        processedEvents.save(new ProcessedWebhookEvent(event.getId(), event.getType(), clock.instant()));

        switch (event.getType()) {
            case "checkout.session.completed", "checkout.session.async_payment_succeeded" -> {
                Session session = session(event);
                if ("paid".equals(session.getPaymentStatus())) {
                    orderService.confirmExternalPayment(orderId(session), session.getPaymentIntent());
                }
            }
            case "checkout.session.expired", "checkout.session.async_payment_failed" ->
                    orderService.expireExternalPayment(orderId(session(event)));
            default -> log.debug("Ignoring Stripe event type {}", event.getType());
        }
    }

    private static Session session(Event event) {
        var deserializer = event.getDataObjectDeserializer();
        StripeObject object = deserializer.getObject().orElseGet(() -> {
            try {
                return deserializer.deserializeUnsafe();
            } catch (EventDataObjectDeserializationException ex) {
                throw new IllegalArgumentException("Evento do Stripe com payload inesperado", ex);
            }
        });
        return (Session) object;
    }

    private static UUID orderId(Session session) {
        return UUID.fromString(session.getClientReferenceId());
    }
}
