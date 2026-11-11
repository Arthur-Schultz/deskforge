package com.deskforge.order;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OrderMetrics {

    private final MeterRegistry registry;
    private final Counter revenue;

    public OrderMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.revenue = Counter.builder("deskforge.orders.revenue")
                .description("Valor total dos pedidos pagos, em reais")
                .baseUnit("BRL")
                .register(registry);
        for (OrderStatus status : OrderStatus.values()) {
            transitions(status);
        }
    }

    private Counter transitions(OrderStatus status) {
        return Counter.builder("deskforge.orders.transitions")
                .description("Pedidos que entraram em cada status")
                .tag("status", status.name())
                .register(registry);
    }

    @EventListener
    public void on(OrderStatusChanged event) {
        OrderStatus status = event.order().getStatus();
        transitions(status).increment();
        if (status == OrderStatus.PAID) {
            revenue.increment(event.order().getTotal().doubleValue());
        }
    }
}
