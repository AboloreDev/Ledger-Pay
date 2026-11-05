package com.abolore.ledger_pay.webhooks.entity;

public enum WebhookOutboxStatus {
    PENDING,
    PROCESSING,
    PUBLISHED,
    FAILED
}