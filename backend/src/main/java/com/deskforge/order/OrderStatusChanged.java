package com.deskforge.order;

public record OrderStatusChanged(Order order, OrderStatus previousStatus) {
}
