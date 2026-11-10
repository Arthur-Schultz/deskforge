package com.deskforge.order;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OrderStatusTest {

    @Test
    void followsTheFulfillmentFlow() {
        assertThat(OrderStatus.AWAITING_PAYMENT.canTransitionTo(OrderStatus.PAID)).isTrue();
        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.SHIPPED)).isTrue();
        assertThat(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.DELIVERED)).isTrue();
    }

    @Test
    void doesNotSkipSteps() {
        assertThat(OrderStatus.AWAITING_PAYMENT.canTransitionTo(OrderStatus.SHIPPED)).isFalse();
        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.DELIVERED)).isFalse();
    }

    @Test
    void canceledIsTerminal() {
        assertThat(OrderStatus.CANCELED.nextStatuses()).isEmpty();
    }
}
