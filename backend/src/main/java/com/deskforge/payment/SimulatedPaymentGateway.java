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
    public String provider() {
        return "simulated";
    }

    @Override
    public PaymentResult charge(Order order) {
        log.info("Simulated payment approved for order {} ({})", order.getNumber(), order.getTotal());
        return PaymentResult.approved("sim_" + UUID.randomUUID().toString().replace("-", ""));
    }

    @Override
    public void refund(String paymentReference, String orderNumber) {
        log.info("Simulated refund issued for order {} ({})", orderNumber, paymentReference);
    }
}
