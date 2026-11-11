package com.deskforge.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.deskforge.catalog.ArtKind;
import com.deskforge.catalog.CategoryRepository;
import com.deskforge.catalog.Product;
import com.deskforge.catalog.ProductRepository;
import com.deskforge.notification.EmailMessageRepository;
import com.deskforge.order.OrderRepository;
import com.deskforge.order.OrderService;
import com.deskforge.order.OrderStatus;
import com.deskforge.order.dto.CheckoutRequest;
import com.deskforge.payment.stripe.ProcessedWebhookEventRepository;
import com.deskforge.support.PostgresIntegrationTest;
import com.deskforge.user.User;
import com.deskforge.user.UserRepository;
import com.stripe.Stripe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = "deskforge.payment.provider=stripe")
class StripeWebhookIT extends PostgresIntegrationTest {

    private static final String WEBHOOK_SECRET = "whsec_" + randomToken();

    @DynamicPropertySource
    static void stripeProperties(DynamicPropertyRegistry registry) {
        registry.add("deskforge.payment.stripe.secret-key", () -> "sk_test_" + randomToken());
        registry.add("deskforge.payment.stripe.webhook-secret", () -> WEBHOOK_SECRET);
    }

    @MockitoBean
    private PaymentGateway paymentGateway;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orders;

    @Autowired
    private ProductRepository products;

    @Autowired
    private CategoryRepository categories;

    @Autowired
    private UserRepository users;

    @Autowired
    private ProcessedWebhookEventRepository processedEvents;

    @Autowired
    private EmailMessageRepository emails;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void completedCheckoutMarksTheOrderAsPaidOnlyOnce() throws Exception {
        Placed placed = placeOrder(3);
        String payload = event("evt_" + UUID.randomUUID(), "checkout.session.completed", placed.orderId(), "paid");

        send(payload).andExpect(status().isOk());
        send(payload).andExpect(status().isOk());

        var order = orders.findById(placed.orderId()).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaymentReference()).isEqualTo("pi_test_" + placed.orderId());
        assertThat(processedEvents.count()).isPositive();
        assertThat(emails.findByDedupKey("order:" + placed.orderId() + ":PAID")).isPresent();
        verify(paymentGateway, never()).refund(anyString(), anyString());
    }

    @Test
    void expiredSessionCancelsTheOrderAndReleasesStock() throws Exception {
        Placed placed = placeOrder(2);

        send(event("evt_" + UUID.randomUUID(), "checkout.session.expired", placed.orderId(), "unpaid"))
                .andExpect(status().isOk());

        assertThat(orders.findById(placed.orderId()).orElseThrow().getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(products.findById(placed.productId()).orElseThrow().getStock()).isEqualTo(2);
    }

    @Test
    void paymentForACanceledOrderIsRefunded() throws Exception {
        Placed placed = placeOrder(1);
        send(event("evt_" + UUID.randomUUID(), "checkout.session.expired", placed.orderId(), "unpaid"))
                .andExpect(status().isOk());

        send(event("evt_" + UUID.randomUUID(), "checkout.session.completed", placed.orderId(), "paid"))
                .andExpect(status().isOk());

        assertThat(orders.findById(placed.orderId()).orElseThrow().getStatus()).isEqualTo(OrderStatus.CANCELED);
        verify(paymentGateway, times(1)).refund(eq("pi_test_" + placed.orderId()), anyString());
    }

    @Test
    void rejectsEventsWithAnInvalidSignature() throws Exception {
        Placed placed = placeOrder(1);
        String payload = event("evt_" + UUID.randomUUID(), "checkout.session.completed", placed.orderId(), "paid");

        mvc.perform(post("/api/payments/stripe/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", "t=" + Instant.now().getEpochSecond() + ",v1=deadbeef")
                        .content(payload))
                .andExpect(status().isBadRequest());

        assertThat(orders.findById(placed.orderId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.AWAITING_PAYMENT);
    }

    private org.springframework.test.web.servlet.ResultActions send(String payload) throws Exception {
        return mvc.perform(post("/api/payments/stripe/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Stripe-Signature", sign(payload))
                .content(payload));
    }

    private Placed placeOrder(int stock) {
        Product product = products.save(Product.create("IT-WH-" + UUID.randomUUID().toString().substring(0, 8),
                "wh-" + UUID.randomUUID(), "Webhook product", "Short", "Long", new BigDecimal("120.00"), null,
                stock, ArtKind.MOUSE, "#FF6B2C", false, categories.findBySlug("mouses").orElseThrow(), List.of(),
                Instant.now()));
        UUID customer = users.save(User.newCustomer("Webhook", "webhook-" + UUID.randomUUID() + "@test.dev", "hash",
                false, "v1", Instant.now())).getId();
        UUID orderId = orderService.checkout(customer, new CheckoutRequest(
                List.of(new CheckoutRequest.Item(product.getId(), 1)),
                new CheckoutRequest.Address("Cliente", "01001-000", "Rua", "1", null, "Centro", "São Paulo", "SP"),
                true)).id();
        return new Placed(orderId, product.getId());
    }

    private static String event(String id, String type, UUID orderId, String paymentStatus) {
        return """
                {"id":"%s","object":"event","api_version":"%s","created":%d,"livemode":false,"type":"%s",
                 "data":{"object":{"id":"cs_test_%s","object":"checkout.session","client_reference_id":"%s",
                 "payment_intent":"pi_test_%s","payment_status":"%s","status":"complete","metadata":{"orderId":"%s"}}}}
                """.formatted(id, Stripe.API_VERSION, Instant.now().getEpochSecond(), type, orderId, orderId, orderId,
                paymentStatus, orderId);
    }

    private static String sign(String payload) throws Exception {
        long timestamp = Instant.now().getEpochSecond();
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(WEBHOOK_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] digest = mac.doFinal((timestamp + "." + payload).getBytes(StandardCharsets.UTF_8));
        return "t=" + timestamp + ",v1=" + HexFormat.of().formatHex(digest);
    }

    private static String randomToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private record Placed(UUID orderId, Long productId) {
    }
}
