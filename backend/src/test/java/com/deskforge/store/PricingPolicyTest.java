package com.deskforge.store;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class PricingPolicyTest {

    private final PricingPolicy pricing = new PricingPolicy(new StoreProperties("DeskForge", "Legal", "cnpj",
            "addr", "sac", "phone", "hours", "dpo", "dpo-mail", "v1", 7, 5, 15, 30,
            new BigDecimal("299.00"), new BigDecimal("24.90"), new BigDecimal("0.2785"), true));

    @Test
    void chargesFlatShippingBelowTheThreshold() {
        assertThat(pricing.shippingFor(new BigDecimal("298.99"))).isEqualByComparingTo("24.90");
    }

    @Test
    void shipsForFreeFromTheThreshold() {
        assertThat(pricing.shippingFor(new BigDecimal("299.00"))).isEqualByComparingTo("0.00");
    }

    @Test
    void estimatesTaxesWithTwoDecimals() {
        assertThat(pricing.approximateTaxesFor(new BigDecimal("100.00"))).isEqualByComparingTo("27.85");
    }
}
