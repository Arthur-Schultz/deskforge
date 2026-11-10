package com.deskforge.payment;

import com.deskforge.order.Order;

public interface PaymentGateway {

    String provider();

    PaymentResult charge(Order order);

    void refund(String paymentReference, String orderNumber);

    default void cancelPending(Order order) {
    }
}
