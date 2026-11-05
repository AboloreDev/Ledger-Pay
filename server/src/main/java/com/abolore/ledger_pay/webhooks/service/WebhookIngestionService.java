package com.abolore.ledger_pay.webhooks.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abolore.ledger_pay.webhooks.entity.WebhookOutbox;
import com.abolore.ledger_pay.webhooks.repository.WebhookOutboxRepository;
import com.abolore.ledger_pay.webhooks.security.PaystackWebhookPayloadValidator;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class WebhookIngestionService {

    private static final String PROVIDER = "PAYSTACK";

    private final WebhookOutboxRepository webhookOutboxRepository;
    private final ObjectMapper objectMapper;
    private final PaystackWebhookPayloadValidator payloadValidator;

    public WebhookIngestionService(WebhookOutboxRepository webhookOutboxRepository, ObjectMapper objectMapper,
            PaystackWebhookPayloadValidator payloadValidator) {
        this.webhookOutboxRepository = webhookOutboxRepository;
        this.objectMapper = objectMapper;
        this.payloadValidator = payloadValidator;
    }

    @Transactional
    public void receiveWebhook(String rawPayload) {

        try {
            var validated = payloadValidator.validate(rawPayload);

            if (!validated.isSupported()) {
                // Valid event, but not handled by this payment pipeline.
                return;
            }

            String eventType = validated.eventType();
            String transactionReference = validated.transactionReference();

            String deduplicationKey = eventType + ":" + validated.providerTransactionId();

            webhookOutboxRepository.insertIfAbsent(
                    UUID.randomUUID(),
                    "PAYSTACK",
                    eventType,
                    deduplicationKey,
                    transactionReference,
                    rawPayload);

        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Unable to process Paystack webhook payload",
                    exception);
        }
    }
}