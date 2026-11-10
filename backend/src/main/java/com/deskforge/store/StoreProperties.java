package com.deskforge.store;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deskforge.store")
public record StoreProperties(
        String tradeName,
        String legalName,
        String cnpj,
        String address,
        String supportEmail,
        String supportPhone,
        String supportHours,
        String dataProtectionOfficer,
        String dataProtectionEmail,
        String privacyPolicyVersion,
        int withdrawalDays,
        int supportResponseDays,
        int dataSubjectResponseDays,
        int paymentWindowMinutes,
        BigDecimal freeShippingThreshold,
        BigDecimal flatShippingCost,
        BigDecimal approximateTaxRate,
        boolean demoMode) {
}
