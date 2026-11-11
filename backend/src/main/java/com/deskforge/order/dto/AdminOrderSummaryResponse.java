package com.deskforge.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.deskforge.demo.PersonalDataMask;
import com.deskforge.order.Order;
import com.deskforge.order.OrderStatus;

public record AdminOrderSummaryResponse(UUID id, String number, String customerName, String customerEmail,
                                        OrderStatus status, Set<OrderStatus> nextStatuses, BigDecimal total,
                                        String city, String state, Instant createdAt) {

    public static AdminOrderSummaryResponse from(Order o) {
        return new AdminOrderSummaryResponse(o.getId(), o.getNumber(), o.getCustomer().getFullName(),
                o.getCustomer().getEmail(), o.getStatus(), o.getStatus().nextStatuses(), o.getTotal(),
                o.getShippingAddress().getCity(), o.getShippingAddress().getState(), o.getCreatedAt());
    }

    public AdminOrderSummaryResponse withMaskedCustomer() {
        return new AdminOrderSummaryResponse(id, number, PersonalDataMask.name(customerName),
                PersonalDataMask.email(customerEmail), status, nextStatuses, total, city, state, createdAt);
    }
}
