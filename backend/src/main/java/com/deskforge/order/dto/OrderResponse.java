package com.deskforge.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.deskforge.demo.PersonalDataMask;
import com.deskforge.order.Order;
import com.deskforge.order.OrderStatus;

public record OrderResponse(UUID id, String number, OrderStatus status, List<OrderItemResponse> items,
                            BigDecimal subtotal, BigDecimal shippingCost, BigDecimal total,
                            BigDecimal approximateTaxes, AddressResponse shippingAddress, String cancelReason,
                            Instant createdAt, Instant paidAt, Instant shippedAt, Instant deliveredAt,
                            Instant canceledAt, Instant expiresAt, Instant withdrawalDeadline, boolean cancelable) {

    public static OrderResponse from(Order o, Instant now, int withdrawalDays) {
        return new OrderResponse(o.getId(), o.getNumber(), o.getStatus(),
                o.getItems().stream().map(OrderItemResponse::from).toList(),
                o.getSubtotal(), o.getShippingCost(), o.getTotal(), o.getApproximateTaxes(),
                AddressResponse.from(o.getShippingAddress()), o.getCancelReason(), o.getCreatedAt(), o.getPaidAt(),
                o.getShippedAt(), o.getDeliveredAt(), o.getCanceledAt(), o.getExpiresAt(),
                o.withdrawalDeadline(withdrawalDays), o.canBeCanceledByCustomer(now, withdrawalDays));
    }

    public OrderResponse withMaskedPersonalData() {
        return new OrderResponse(id, number, status, items, subtotal, shippingCost, total, approximateTaxes,
                shippingAddress.masked(), PersonalDataMask.hidden(cancelReason), createdAt, paidAt, shippedAt,
                deliveredAt, canceledAt, expiresAt, withdrawalDeadline, cancelable);
    }
}
