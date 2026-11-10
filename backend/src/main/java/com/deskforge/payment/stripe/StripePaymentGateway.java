package com.deskforge.payment.stripe;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import com.deskforge.order.Order;
import com.deskforge.order.OrderItem;
import com.deskforge.payment.PaymentGateway;
import com.deskforge.payment.PaymentProviderException;
import com.deskforge.payment.PaymentResult;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "deskforge.payment.provider", havingValue = "stripe")
public class StripePaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentGateway.class);
    private static final String CURRENCY = "brl";
    private static final String SESSION_PREFIX = "cs_";
    private static final Duration SESSION_LIFETIME = Duration.ofMinutes(60);

    private final StripeClient client;
    private final String frontendUrl;

    public StripePaymentGateway(StripeProperties properties, @Value("${deskforge.frontend-url}") String frontendUrl) {
        this.client = new StripeClient(requireTestKey(properties.secretKey()));
        this.frontendUrl = frontendUrl;
    }

    @Override
    public String provider() {
        return "stripe";
    }

    @Override
    public PaymentResult charge(Order order) {
        try {
            if (isSession(order.getPaymentReference())) {
                Session existing = client.v1().checkout().sessions().retrieve(order.getPaymentReference());
                if ("open".equals(existing.getStatus())) {
                    return PaymentResult.redirect(existing.getId(), existing.getUrl(),
                            Instant.ofEpochSecond(existing.getExpiresAt()));
                }
            }
            Instant expiresAt = order.getCreatedAt().plus(SESSION_LIFETIME);
            Session session = client.v1().checkout().sessions().create(sessionParams(order, expiresAt),
                    RequestOptions.builder().setIdempotencyKey("checkout-" + order.getId()).build());
            log.info("Stripe Checkout session {} created for order {}", session.getId(), order.getNumber());
            return PaymentResult.redirect(session.getId(), session.getUrl(), expiresAt);
        } catch (StripeException ex) {
            throw new PaymentProviderException("Não foi possível iniciar o pagamento com o Stripe", ex);
        }
    }

    @Override
    public void refund(String paymentReference, String orderNumber) {
        try {
            client.v1().refunds().create(
                    RefundCreateParams.builder()
                            .setPaymentIntent(paymentReference)
                            .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                            .build(),
                    RequestOptions.builder().setIdempotencyKey("refund-" + orderNumber).build());
            log.info("Stripe refund requested for order {}", orderNumber);
        } catch (StripeException ex) {
            throw new PaymentProviderException("Não foi possível solicitar o reembolso ao Stripe", ex);
        }
    }

    @Override
    public void cancelPending(Order order) {
        if (!isSession(order.getPaymentReference())) {
            return;
        }
        try {
            client.v1().checkout().sessions().expire(order.getPaymentReference());
        } catch (StripeException ex) {
            log.warn("Could not expire Stripe session for order {}: {}", order.getNumber(), ex.getMessage());
        }
    }

    private SessionCreateParams sessionParams(Order order, Instant expiresAt) {
        String orderUrl = frontendUrl + "/conta/pedidos/" + order.getId();
        SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setLocale(SessionCreateParams.Locale.PT_BR)
                .setClientReferenceId(order.getId().toString())
                .setCustomerEmail(order.getCustomer().getEmail())
                .setExpiresAt(expiresAt.getEpochSecond())
                .setSuccessUrl(orderUrl + "?pagamento=sucesso")
                .setCancelUrl(orderUrl + "?pagamento=cancelado")
                .putMetadata("orderId", order.getId().toString())
                .putMetadata("orderNumber", order.getNumber());
        for (OrderItem item : order.getItems()) {
            builder.addLineItem(lineItem(item.getProductName(), item.getUnitPrice(), item.getQuantity()));
        }
        if (order.getShippingCost().signum() > 0) {
            builder.addLineItem(lineItem("Frete", order.getShippingCost(), 1));
        }
        return builder.build();
    }

    private static SessionCreateParams.LineItem lineItem(String name, BigDecimal unitPrice, int quantity) {
        return SessionCreateParams.LineItem.builder()
                .setQuantity((long) quantity)
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(CURRENCY)
                        .setUnitAmount(unitPrice.movePointRight(2).longValueExact())
                        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName(name)
                                .build())
                        .build())
                .build();
    }

    private static boolean isSession(String reference) {
        return reference != null && reference.startsWith(SESSION_PREFIX);
    }

    private static String requireTestKey(String secretKey) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("STRIPE_SECRET_KEY não definido no .env");
        }
        if (!secretKey.startsWith("sk_test_") && !secretKey.startsWith("rk_test_")) {
            throw new IllegalStateException("Este projeto aceita apenas chaves de teste do Stripe (sk_test_...)");
        }
        return secretKey;
    }
}
