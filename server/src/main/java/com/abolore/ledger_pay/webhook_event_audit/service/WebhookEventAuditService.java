package com.abolore.ledger_pay.webhook_event_audit.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abolore.ledger_pay.webhook_event_audit.entity.WebhookEventAudit;
import com.abolore.ledger_pay.webhook_event_audit.repository.WebhookEventAuditRepository;

@Service
public class WebhookEventAuditService {

    private final WebhookEventAuditRepository auditRepository;

    public WebhookEventAuditService(WebhookEventAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Transactional
    public boolean createAuditIfAbsent(String provider, String deduplicationKey, String eventType,
            String transactionReference, String payload) {

        int inserted = auditRepository.insertIfAbsent(UUID.randomUUID(), provider, deduplicationKey, eventType,
                transactionReference, payload, Instant.now());

        return inserted == 1;
    }

    @Transactional
    public void markProcessing(String provider, String deduplicationKey) {
        WebhookEventAudit audit = findAudit(provider, deduplicationKey);
        audit.markProcessing();
    }

    @Transactional
    public void markProcessed(String provider, String deduplicationKey) {
        WebhookEventAudit audit = findAudit(provider, deduplicationKey);
        audit.markProcessed();
    }

    @Transactional
    public void markIgnored(String provider, String deduplicationKey, String reason) {

        WebhookEventAudit audit = findAudit(provider, deduplicationKey);
        audit.markIgnored(reason);
    }

    @Transactional
    public void markFailed(String provider, String deduplicationKey, String error) {

        WebhookEventAudit audit = findAudit(provider, deduplicationKey);
        audit.markFailed(error);
    }

    public WebhookEventAudit findAudit(String provider, String deduplicationKey) {

        return auditRepository
                .findByProviderAndDeduplicationKey(provider, deduplicationKey)
                .orElseThrow(() -> new IllegalStateException(
                        "Webhook audit record not found: " + deduplicationKey));
    }

}