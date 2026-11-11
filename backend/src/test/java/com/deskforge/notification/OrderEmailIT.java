package com.deskforge.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.deskforge.catalog.ArtKind;
import com.deskforge.catalog.CategoryRepository;
import com.deskforge.catalog.Product;
import com.deskforge.catalog.ProductRepository;
import com.deskforge.order.OrderService;
import com.deskforge.order.OrderStatus;
import com.deskforge.order.dto.CheckoutRequest;
import com.deskforge.privacy.PrivacyService;
import com.deskforge.support.MailContent;
import com.deskforge.support.PostgresIntegrationTest;
import com.deskforge.user.User;
import com.deskforge.user.UserRepository;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;

class OrderEmailIT extends PostgresIntegrationTest {

    @RegisterExtension
    static final GreenMailExtension GREEN_MAIL = new GreenMailExtension(ServerSetupTest.SMTP);

    @Autowired
    private OrderService orderService;

    @Autowired
    private EmailDispatcher dispatcher;

    @Autowired
    private EmailMessageRepository emails;

    @Autowired
    private ProductRepository products;

    @Autowired
    private CategoryRepository categories;

    @Autowired
    private UserRepository users;

    @Autowired
    private PrivacyService privacyService;

    @Test
    void paymentConfirmationIsQueuedWithTheOrderAndSentOnlyOnce() throws Exception {
        Placed placed = placeOrder();

        orderService.pay(placed.customerId(), placed.orderId());

        EmailMessage queued = emails.findByDedupKey("order:" + placed.orderId() + ":PAID").orElseThrow();
        assertThat(queued.getStatus()).isEqualTo(EmailStatus.PENDING);
        assertThat(queued.getTemplate()).isEqualTo(EmailTemplate.PAYMENT_CONFIRMED);

        dispatcher.dispatchDue();
        dispatcher.dispatchDue();

        List<MimeMessage> received = receivedBy(placed.email());
        assertThat(received).hasSize(1);
        MimeMessage message = received.getFirst();
        assertThat(message.getSubject()).startsWith("Pagamento confirmado: pedido DF-");
        assertThat(message.getFrom()[0].toString()).contains("nao-responda@deskforge.dev");
        assertThat(MailContent.text(message))
                .contains("Oi, Ana.", "Mesa de teste x 2: R$ 240,00", "Total: R$ 264,90", "/conta/pedidos/" + placed.orderId());
        assertThat(MailContent.html(message))
                .contains("Pagamento confirmado", "Mesa de teste", "href=\"http://localhost:5173/conta/pedidos/" + placed.orderId());

        EmailMessage sent = emails.findById(queued.getId()).orElseThrow();
        assertThat(sent.getStatus()).isEqualTo(EmailStatus.SENT);
        assertThat(sent.getHtmlBody()).isNull();
    }

    @Test
    void shippingAndCancellationNotifyTheCustomer() throws Exception {
        Placed placed = placeOrder();
        orderService.pay(placed.customerId(), placed.orderId());
        orderService.updateStatus(placed.orderId(), OrderStatus.SHIPPED);
        orderService.cancelByCustomer(placed.customerId(), placed.orderId(), null);

        dispatcher.dispatchDue();

        List<String> subjects = receivedBy(placed.email()).stream().map(OrderEmailIT::subject).toList();
        assertThat(subjects).hasSize(3);
        assertThat(subjects.get(1)).endsWith("foi enviado");
        assertThat(subjects.get(2)).endsWith("cancelado");
        assertThat(MailContent.text(receivedBy(placed.email()).get(2))).contains("estorno de R$ 264,90");
    }

    @Test
    void unpaidOrdersCanceledByTheStoreDoNotMentionARefund() throws Exception {
        Placed placed = placeOrder();
        orderService.updateStatus(placed.orderId(), OrderStatus.CANCELED);

        dispatcher.dispatchDue();

        List<MimeMessage> received = receivedBy(placed.email());
        assertThat(received).hasSize(1);
        assertThat(MailContent.text(received.getFirst()))
                .contains("Motivo: Cancelado pela loja", "Nenhuma cobrança foi feita.")
                .doesNotContain("estorno");
    }

    @Test
    void deletingTheAccountRemovesTheAddressFromTheEmailHistory() {
        Placed placed = placeOrder();
        orderService.pay(placed.customerId(), placed.orderId());
        dispatcher.dispatchDue();
        orderService.updateStatus(placed.orderId(), OrderStatus.SHIPPED);

        privacyService.anonymize(placed.customerId());

        assertThat(emails.findAllByRecipientOrderByCreatedAtAsc(placed.email())).isEmpty();
        assertThat(emails.findByDedupKey("order:" + placed.orderId() + ":SHIPPED")).isEmpty();
        EmailMessage sent = emails.findByDedupKey("order:" + placed.orderId() + ":PAID").orElseThrow();
        assertThat(sent.getRecipient()).endsWith("@anonymized.invalid");
        assertThat(sent.getStatus()).isEqualTo(EmailStatus.SENT);
    }

    private List<MimeMessage> receivedBy(String email) {
        return Arrays.stream(GREEN_MAIL.getReceivedMessages())
                .filter(message -> recipients(message).contains(email))
                .toList();
    }

    private static String recipients(MimeMessage message) {
        try {
            return Arrays.toString(message.getRecipients(Message.RecipientType.TO));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String subject(MimeMessage message) {
        try {
            return message.getSubject();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private Placed placeOrder() {
        Product product = products.save(Product.create("IT-EM-" + UUID.randomUUID().toString().substring(0, 8),
                "em-" + UUID.randomUUID(), "Mesa de teste", "Short", "Long", new BigDecimal("120.00"), null,
                5, ArtKind.DESKMAT, "#2747E0", false, categories.findBySlug("mouses").orElseThrow(), List.of(),
                Instant.now()));
        String email = "email-" + UUID.randomUUID() + "@test.dev";
        UUID customerId = users.save(User.newCustomer("Ana Teste", email, "hash", false, "v1", Instant.now())).getId();
        UUID orderId = orderService.checkout(customerId, new CheckoutRequest(
                List.of(new CheckoutRequest.Item(product.getId(), 2)),
                new CheckoutRequest.Address("Ana Teste", "01001-000", "Rua", "1", null, "Centro", "São Paulo", "SP"),
                true)).id();
        return new Placed(orderId, customerId, email);
    }

    private record Placed(UUID orderId, UUID customerId, String email) {
    }
}
