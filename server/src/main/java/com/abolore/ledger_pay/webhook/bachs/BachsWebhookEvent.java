package com.abolore.ledger_pay.webhook.bachs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import tools.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BachsWebhookEvent(
        String id,
        String type,
        JsonNode data) {
}