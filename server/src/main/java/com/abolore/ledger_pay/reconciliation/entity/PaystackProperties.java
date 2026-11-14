package com.abolore.ledger_pay.reconciliation.entity;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "paystack")
public record PaystackProperties(String apiBaseUrl, String apiSecretKey,
        @DefaultValue ReconciliationRun reconciliationRun) {
    public record ReconciliationRun(@DefaultValue("50") int perPage) {
    }
}
