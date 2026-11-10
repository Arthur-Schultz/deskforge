package com.deskforge.payment;

import java.util.UUID;

import com.deskforge.order.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "deskforge.payment.provider", havingValue = "simulated", matchIfMissing = true)
public class SimulatedPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(SimulatedPaymentGateway.class);

    @Override
    public PaymentResult charge(Order order) {
        String reference = "sim_" + UUID.randomUUID().toString().replace("-", "");
        log.info("Simulated payment approved for order {} ({})", order.getNumber(), order.getTotal());
        return new PaymentResult(true, reference, null);
    }

    @Override
    public void refund(Order order) {
        log.info("Simulated refund issued for order {} ({})", order.getNumber(), order.getPaymentReference());
    }
}
