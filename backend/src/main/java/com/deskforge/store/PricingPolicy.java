package com.deskforge.store;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

@Component
public class PricingPolicy {

    private final StoreProperties properties;

    public PricingPolicy(StoreProperties properties) {
        this.properties = properties;
    }

    public BigDecimal shippingFor(BigDecimal subtotal) {
        if (subtotal.compareTo(properties.freeShippingThreshold()) >= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return properties.flatShippingCost().setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal approximateTaxesFor(BigDecimal total) {
        return total.multiply(properties.approximateTaxRate()).setScale(2, RoundingMode.HALF_UP);
    }
}
