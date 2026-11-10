package com.deskforge.order.dto;

public record PaymentResponse(OrderResponse order, String redirectUrl) {
}
