package com.deskforge.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.deskforge.catalog.ArtKind;
import com.deskforge.catalog.CategoryRepository;
import com.deskforge.catalog.Product;
import com.deskforge.catalog.ProductRepository;
import com.deskforge.order.dto.CheckoutRequest;
import com.deskforge.shared.exception.ConflictException;
import com.deskforge.support.PostgresIntegrationTest;
import com.deskforge.user.User;
import com.deskforge.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CheckoutConcurrencyIT extends PostgresIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository products;

    @Autowired
    private CategoryRepository categories;

    @Autowired
    private UserRepository users;

    @Test
    void onlyOneCustomerGetsTheLastUnit() throws Exception {
        Product lastUnit = products.save(Product.create(sku("LAST"), "last-" + UUID.randomUUID(),
                "Last unit", "Short", "Long", new BigDecimal("100.00"), null, 1, ArtKind.MOUSE, "#FF6B2C",
                false, categories.findBySlug("mouses").orElseThrow(), List.of(), Instant.now()));
        int buyers = 8;
        List<UUID> customers = new ArrayList<>();
        for (int i = 0; i < buyers; i++) {
            customers.add(users.save(User.newCustomer("Buyer " + i, "buyer" + i + "-" + UUID.randomUUID() + "@test.dev",
                    "hash", false, "v1", Instant.now())).getId());
        }

        CountDownLatch start = new CountDownLatch(1);
        List<Callable<Boolean>> attempts = customers.stream().<Callable<Boolean>>map(customerId -> () -> {
            start.await();
            try {
                orderService.checkout(customerId, request(lastUnit.getId()));
                return true;
            } catch (ConflictException ex) {
                return false;
            }
        }).toList();

        int successes = 0;
        try (ExecutorService pool = Executors.newFixedThreadPool(buyers)) {
            List<Future<Boolean>> results = attempts.stream().map(pool::submit).toList();
            start.countDown();
            for (Future<Boolean> result : results) {
                if (result.get()) {
                    successes++;
                }
            }
        }

        assertThat(successes).isEqualTo(1);
        assertThat(products.findById(lastUnit.getId()).orElseThrow().getStock()).isZero();
    }

    @Test
    void cancelingAPaidOrderReturnsTheStock() {
        Product product = products.save(Product.create(sku("CANCEL"), "cancel-" + UUID.randomUUID(),
                "Cancelable", "Short", "Long", new BigDecimal("80.00"), null, 5, ArtKind.DESKMAT, "#FF6B2C",
                false, categories.findBySlug("desk-mats").orElseThrow(), List.of(), Instant.now()));
        UUID customer = users.save(User.newCustomer("Cancel", "cancel-" + UUID.randomUUID() + "@test.dev", "hash",
                false, "v1", Instant.now())).getId();

        UUID orderId = orderService.checkout(customer, request(product.getId())).id();
        orderService.pay(customer, orderId);
        assertThat(products.findById(product.getId()).orElseThrow().getStock()).isEqualTo(4);

        var canceled = orderService.cancelByCustomer(customer, orderId, null);

        assertThat(canceled.status()).isEqualTo(OrderStatus.CANCELED);
        assertThat(products.findById(product.getId()).orElseThrow().getStock()).isEqualTo(5);
    }

    private static String sku(String prefix) {
        return "IT-" + prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private static CheckoutRequest request(Long productId) {
        return new CheckoutRequest(List.of(new CheckoutRequest.Item(productId, 1)),
                new CheckoutRequest.Address("Cliente", "01001-000", "Rua", "1", null, "Centro", "São Paulo", "SP"),
                true);
    }
}
