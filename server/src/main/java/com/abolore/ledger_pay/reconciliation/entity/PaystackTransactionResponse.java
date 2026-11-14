package com.abolore.ledger_pay.reconciliation.entity;

import java.time.Instant;

public record PaystackTransactionResponse(Long id, String reference, Long amount, String currency, String status,
        Instant createdAt, Instant paidAt) {
}