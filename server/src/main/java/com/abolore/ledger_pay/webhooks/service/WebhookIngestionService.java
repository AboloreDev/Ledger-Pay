package com.abolore.ledger_pay.webhooks.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abolore.ledger_pay.webhook_event_audit.service.WebhookEventAuditService;
import com.abolore.ledger_pay.webhooks.entity.WebhookOutbox;
import com.abolore.ledger_pay.webhooks.repository.WebhookOutboxRepository;
import com.abolore.ledger_pay.webhooks.security.PaystackWebhookPayloadValidator;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class WebhookIngestionService {

    private static final String PROVIDER = "PAYSTACK";

    private final WebhookOutboxRepository webhookOutboxRepository;
    private final PaystackWebhookPayloadValidator payloadValidator;
    private final WebhookEventAuditService webhookEventAuditService;

    public WebhookIngestionService(WebhookOutboxRepository webhookOutboxRepository,
            PaystackWebhookPayloadValidator payloadValidator, WebhookEventAuditService webhookEventAuditService) {
        this.webhookOutboxRepository = webhookOutboxRepository;
        this.payloadValidator = payloadValidator;
        this.webhookEventAuditService = webhookEventAuditService;
    }

    @Transactional
    public void receiveWebhook(String rawPayload) {

        try {
            var validated = payloadValidator.validate(rawPayload);

            String eventType = validated.eventType();
            String transactionReference = validated.transactionReference();

            String deduplicationKey = eventType + ":" + validated.providerTransactionId();

            webhookEventAuditService.createAudit(
                    PROVIDER,
                    deduplicationKey,
                    eventType,
                    transactionReference,
                    rawPayload);

            if (!validated.isSupported()) {
                return;
            }

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