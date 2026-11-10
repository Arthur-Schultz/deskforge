package com.deskforge.payment;

import com.deskforge.order.Order;

public interface PaymentGateway {

    PaymentResult charge(Order order);

    void refund(Order order);
}
