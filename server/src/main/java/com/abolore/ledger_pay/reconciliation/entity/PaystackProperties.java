package com.abolore.ledger_pay.reconciliation.entity;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paystack")
public record PaystackProperties(Api api, Reconciliation reconciliationRun) {

    public record Api(String baseUrl, String secretKey) {
    }

    public record Reconciliation(int perPage) {
    }
}
