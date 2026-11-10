package com.deskforge.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.deskforge.catalog.ArtKind;
import com.deskforge.catalog.Product;
import com.deskforge.shared.exception.BusinessRuleException;
import com.deskforge.shared.exception.ConflictException;
import com.deskforge.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderTest {

    private static final Instant NOW = Instant.parse("2026-11-10T12:00:00Z");
    private static final int WITHDRAWAL_DAYS = 7;

    private Product keyboard;
    private Order order;

    @BeforeEach
    void setUp() {
        keyboard = Product.create("SKU-1", "keyboard", "Keyboard", "Short", "Long", new BigDecimal("500.00"), null,
                3, ArtKind.KEYBOARD, "#FF6B2C", false, null, List.of(), NOW);
        User customer = User.newCustomer("Cliente", "cliente@test.dev", "hash", false, "v1", NOW);
        ShippingAddress address = new ShippingAddress("Cliente", "01001-000", "Rua", "1", null, "Centro",
                "São Paulo", "SP");
        order = Order.place("DF-1", customer, address, NOW, Duration.ofMinutes(30));
    }

    @Test
    void reservesStockAndComputesSubtotal() {
        order.addItem(keyboard, 2);

        assertThat(keyboard.getStock()).isEqualTo(1);
        assertThat(order.getSubtotal()).isEqualByComparingTo("1000.00");
    }

    @Test
    void rejectsQuantityAboveStock() {
        assertThatThrownBy(() -> order.addItem(keyboard, 4)).isInstanceOf(ConflictException.class);
        assertThat(keyboard.getStock()).isEqualTo(3);
    }

    @Test
    void cancelingReleasesStockAndSignalsRefundWhenPaid() {
        order.addItem(keyboard, 2);
        order.markPaid("ref", NOW.plusSeconds(60));

        boolean refund = order.cancel("motivo", NOW.plusSeconds(120));

        assertThat(refund).isTrue();
        assertThat(keyboard.getStock()).isEqualTo(3);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    void rejectsPaymentAfterTheWindow() {
        order.addItem(keyboard, 1);

        assertThatThrownBy(() -> order.markPaid("ref", NOW.plus(Duration.ofMinutes(31))))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void allowsWithdrawalUpToSevenDaysAfterDelivery() {
        order.addItem(keyboard, 1);
        order.markPaid("ref", NOW);
        order.advanceTo(OrderStatus.SHIPPED, NOW.plus(Duration.ofDays(1)));
        Instant delivered = NOW.plus(Duration.ofDays(3));
        order.advanceTo(OrderStatus.DELIVERED, delivered);

        assertThat(order.canBeCanceledByCustomer(delivered.plus(Duration.ofDays(7)), WITHDRAWAL_DAYS)).isTrue();
        assertThat(order.canBeCanceledByCustomer(delivered.plus(Duration.ofDays(8)), WITHDRAWAL_DAYS)).isFalse();
    }

    @Test
    void blocksInvalidTransitions() {
        order.addItem(keyboard, 1);

        assertThatThrownBy(() -> order.advanceTo(OrderStatus.SHIPPED, NOW))
                .isInstanceOf(BusinessRuleException.class);
    }
}
