package com.deskforge.order;

import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {
    AWAITING_PAYMENT,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELED;

    public Set<OrderStatus> nextStatuses() {
        return switch (this) {
            case AWAITING_PAYMENT -> EnumSet.of(PAID, CANCELED);
            case PAID -> EnumSet.of(SHIPPED, CANCELED);
            case SHIPPED -> EnumSet.of(DELIVERED, CANCELED);
            case DELIVERED -> EnumSet.of(CANCELED);
            case CANCELED -> EnumSet.noneOf(OrderStatus.class);
        };
    }

    public boolean canTransitionTo(OrderStatus target) {
        return nextStatuses().contains(target);
    }

    public boolean isPaid() {
        return this == PAID || this == SHIPPED || this == DELIVERED;
    }
}
