package com.abolore.ledger_pay.webhooks.dto;

public record PaystackWebhookEvent(
        String event,
        Data data) {
    public record Data(
            String reference) {
    }
}