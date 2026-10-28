
package com.abolore.ledger_pay.outbox.entity;

public enum OutboxEventStatus {
    PENDING,
    PROCESSING,
    PROCESSED,
    FAILED
}
