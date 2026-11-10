package com.deskforge.order.dto;

import java.math.BigDecimal;

import com.deskforge.order.OrderItem;

public record OrderItemResponse(Long productId, String productName, String productSku, BigDecimal unitPrice,
                                int quantity, BigDecimal lineTotal) {

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(item.getProduct().getId(), item.getProductName(), item.getProductSku(),
                item.getUnitPrice(), item.getQuantity(), item.getLineTotal());
    }
}
