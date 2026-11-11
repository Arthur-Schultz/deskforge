package com.deskforge.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.deskforge.catalog.ArtKind;
import com.deskforge.catalog.CategoryRepository;
import com.deskforge.catalog.Product;
import com.deskforge.catalog.ProductRepository;
import com.deskforge.notification.EmailMessageRepository;
import com.deskforge.order.OrderService;
import com.deskforge.order.dto.CheckoutRequest;
import com.deskforge.support.PostgresIntegrationTest;
import com.deskforge.user.User;
import com.deskforge.user.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = {
        "deskforge.demo.public-demo=true",
        "deskforge.mail.suppressed-domains=blocked.test"
})
class PublicDemoIT extends PostgresIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository products;

    @Autowired
    private CategoryRepository categories;

    @Autowired
    private UserRepository users;

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
    void theSharedAdminCanLookButNotChangeAnything() throws Exception {
        String admin = token("admin@deskforge.dev", "Admin@2026");

        mvc.perform(get("/api/admin/products").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/admin/products/1/stock").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"stock\":0}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.type").value("https://deskforge.dev/problems/demo-restriction"));
        assertThat(products.findById(1L).orElseThrow().getStock()).isPositive();
    }

    @Test
    void visitorsPersonalDataIsMaskedInThePanel() throws Exception {
        UUID orderId = placeOrderFor("Joana Pereira", "joana-" + UUID.randomUUID() + "@blocked.test");
        String admin = token("admin@deskforge.dev", "Admin@2026");

        String list = mvc.perform(get("/api/admin/orders?size=100").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(list).doesNotContain("Joana Pereira", "joana-").contains("J•••• P••••••");

        mvc.perform(get("/api/admin/orders/" + orderId).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shippingAddress.recipientName").value("J•••• P••••••"))
                .andExpect(jsonPath("$.shippingAddress.street").value("•••"))
                .andExpect(jsonPath("$.shippingAddress.city").value("São Paulo"));
    }

    @Test
    void theSharedCustomerAccountCannotBeDeletedOrTakenOver() throws Exception {
        String customer = token("cliente@deskforge.dev", "Cliente@2026");

        mvc.perform(delete("/api/me").header(HttpHeaders.AUTHORIZATION, customer))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/logout-all").header(HttpHeaders.AUTHORIZATION, customer))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/password/forgot").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cliente@deskforge.dev\"}"))
                .andExpect(status().isAccepted());

        assertThat(emails.findAllByRecipientOrderByCreatedAtAsc("cliente@deskforge.dev"))
                .noneMatch(message -> message.getDedupKey().startsWith("password-reset:"));
        mvc.perform(get("/api/store"))
                .andExpect(jsonPath("$.publicDemo").value(true))
                .andExpect(jsonPath("$.sharedDemoAccounts[0]").value("cliente@deskforge.dev"));
    }

    @Test
    void suppressedDomainsNeverReceiveEmails() {
        String email = "x-" + UUID.randomUUID() + "@blocked.test";
        UUID orderId = placeOrderFor("Xis Teste", email);
        UUID customerId = users.findByEmail(email).orElseThrow().getId();

        orderService.pay(customerId, orderId);

        assertThat(emails.findAllByRecipientOrderByCreatedAtAsc(email)).isEmpty();
    }

    private UUID placeOrderFor(String name, String email) {
        Product product = products.save(Product.create("IT-DM-" + UUID.randomUUID().toString().substring(0, 8),
                "dm-" + UUID.randomUUID(), "Produto demo", "Short", "Long", new BigDecimal("50.00"), null,
                5, ArtKind.MOUSE, "#2747E0", false, categories.findBySlug("mouses").orElseThrow(), List.of(),
                Instant.now()));
        UUID customerId = users.save(User.newCustomer(name, email, "hash", false, "v1", Instant.now())).getId();
        return orderService.checkout(customerId, new CheckoutRequest(
                List.of(new CheckoutRequest.Item(product.getId(), 1)),
                new CheckoutRequest.Address(name, "01001-000", "Rua Secreta", "42", null, "Centro", "São Paulo", "SP"),
                true)).id();
    }

    private String token(String email, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.accessToken");
    }
}
