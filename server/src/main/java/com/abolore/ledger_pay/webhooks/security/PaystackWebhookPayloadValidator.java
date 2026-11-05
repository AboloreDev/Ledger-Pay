package com.abolore.ledger_pay.webhooks.security;

import java.io.IOException;
import java.util.Set;

import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class PaystackWebhookPayloadValidator {

    private static final Set<String> SUPPORTED_EVENTS = Set.of("charge.success");

    private final ObjectMapper objectMapper;

    public PaystackWebhookPayloadValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ValidatedWebhook validate(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            throw new IllegalArgumentException("Webhook payload is empty");
        }

        final JsonNode root;

        try {
            root = objectMapper.readTree(rawPayload);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException(
                    "Webhook payload must be valid JSON",
                    exception);
        }

        if (root == null || !root.isObject()) {
            throw new IllegalArgumentException(
                    "Webhook payload must be a JSON object");
        }

        JsonNode eventNode = root.get("event");

        if (eventNode == null || !eventNode.isTextual() || eventNode.asText().isBlank()) {
            throw new IllegalArgumentException("Missing or invalid event type");
        }

        String eventType = eventNode.asText();

        // Valid event, but not one this payment pipeline handles.
        if (!SUPPORTED_EVENTS.contains(eventType)) {
            return new ValidatedWebhook(eventType, null, null);
        }

        JsonNode data = root.get("data");

        if (data == null || !data.isObject()) {
            throw new IllegalArgumentException(
                    "Missing or invalid transaction data");
        }

        JsonNode idNode = data.get("id");
        JsonNode referenceNode = data.get("reference");
        JsonNode statusNode = data.get("status");
        JsonNode amountNode = data.get("amount");
        JsonNode currencyNode = data.get("currency");

        if (idNode == null || !idNode.isIntegralNumber() || !idNode.canConvertToLong() || idNode.longValue() <= 0) {
            throw new IllegalArgumentException(
                    "Invalid provider transaction ID");
        }

        if (referenceNode == null || !referenceNode.isTextual() || referenceNode.asText().isBlank()) {
            throw new IllegalArgumentException(
                    "Missing transaction reference");
        }

        if (statusNode == null || !statusNode.isTextual() || !"success".equals(statusNode.asText())) {
            throw new IllegalArgumentException(
                    "Invalid charge.success transaction status");
        }

        if (amountNode == null || !(amountNode.isNumber() || amountNode.isTextual()) || amountNode.asText().isBlank()) {
            throw new IllegalArgumentException(
                    "Missing or invalid transaction amount");
        }

        if (currencyNode == null || !currencyNode.isTextual() || !currencyNode.asText().matches("[A-Z]{3}")) {
            throw new IllegalArgumentException(
                    "Invalid transaction currency");
        }

        return new ValidatedWebhook(eventType, idNode.asText(), referenceNode.asText());
    }

    public record ValidatedWebhook(String eventType, String providerTransactionId, String transactionReference) {

        public boolean isSupported() {
            return providerTransactionId != null;
        }
    }
}
