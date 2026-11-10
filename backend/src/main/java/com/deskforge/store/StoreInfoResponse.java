package com.deskforge.store;

import java.math.BigDecimal;

public record StoreInfoResponse(
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
        boolean demoMode,
        String paymentProvider) {

    public static StoreInfoResponse from(StoreProperties p, String paymentProvider) {
        return new StoreInfoResponse(p.tradeName(), p.legalName(), p.cnpj(), p.address(), p.supportEmail(),
                p.supportPhone(), p.supportHours(), p.dataProtectionOfficer(), p.dataProtectionEmail(),
                p.privacyPolicyVersion(), p.withdrawalDays(), p.supportResponseDays(), p.dataSubjectResponseDays(),
                p.paymentWindowMinutes(), p.freeShippingThreshold(), p.flatShippingCost(), p.approximateTaxRate(),
                p.demoMode(), paymentProvider);
    }
}
