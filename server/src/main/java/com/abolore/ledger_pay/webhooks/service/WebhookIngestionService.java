package com.abolore.ledger_pay.webhooks.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abolore.ledger_pay.webhook_event_audit.repository.WebhookEventAuditRepository;

import com.abolore.ledger_pay.webhooks.repository.WebhookOutboxRepository;
import com.abolore.ledger_pay.webhooks.security.PaystackWebhookPayloadValidator;

@Service
public class WebhookIngestionService {

    private static final String PROVIDER = "PAYSTACK";

    private final WebhookOutboxRepository webhookOutboxRepository;
    private final PaystackWebhookPayloadValidator payloadValidator;

    private final WebhookEventAuditRepository webhookEventAuditRepository;

    public WebhookIngestionService(WebhookOutboxRepository webhookOutboxRepository,
            PaystackWebhookPayloadValidator payloadValidator,
            WebhookEventAuditRepository webhookEventAuditRepository) {
        this.webhookOutboxRepository = webhookOutboxRepository;
        this.payloadValidator = payloadValidator;

        this.webhookEventAuditRepository = webhookEventAuditRepository;

    }

    @Transactional
    public void receiveWebhook(String rawPayload) {

        try {
            var validated = payloadValidator.validate(rawPayload);

            String eventType = validated.eventType();
            String transactionReference = validated.transactionReference();

            String deduplicationKey = eventType + ":" + validated.providerTransactionId();

            if (!validated.isSupported()) {
                return;
            }

            webhookEventAuditRepository.insertIfAbsent(
                    UUID.randomUUID(),
                    PROVIDER,
                    deduplicationKey,
                    eventType,
                    transactionReference,
                    rawPayload,
                    Instant.now());

            webhookOutboxRepository.insertIfAbsent(
                    UUID.randomUUID(),
                    PROVIDER,
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