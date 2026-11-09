package com.abolore.ledger_pay.webhook_event_audit.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "webhook_event_audit", uniqueConstraints = {
        @UniqueConstraint(name = "uq_webhook_audit_provider_event", columnNames = { "provider", "deduplication_key" })
})
public class WebhookEventAudit {

    @Id
    private UUID id;

    private String provider;

    @Column(name = "deduplication_key")
    private String deduplicationKey;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "transaction_reference")
    private String transactionReference;

    @Column(columnDefinition = "jsonb")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status")
    private WebhookEventAuditStatus processingStatus;

    private int attempts;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "received_at")
    private Instant receivedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    protected WebhookEventAudit() {
    }

    public static WebhookEventAudit create(String provider, String deduplicationKey, String eventType,
            String transactionReference, String payload) {

        WebhookEventAudit audit = new WebhookEventAudit();
        audit.id = UUID.randomUUID();
        audit.provider = provider;
        audit.deduplicationKey = deduplicationKey;
        audit.eventType = eventType;
        audit.transactionReference = transactionReference;
        audit.payload = payload;
        audit.processingStatus = WebhookEventAuditStatus.RECEIVED;
        audit.attempts = 0;
        audit.receivedAt = Instant.now();

        return audit;
    }

    public void markProcessing() {
        this.processingStatus = WebhookEventAuditStatus.PROCESSING;
        this.attempts++;
        this.lastError = null;
    }

    public void markProcessed() {
        this.processingStatus = WebhookEventAuditStatus.PROCESSED;
        this.processedAt = Instant.now();
        this.lastError = null;
    }

    public void markIgnored(String reason) {
        this.processingStatus = WebhookEventAuditStatus.IGNORED;
        this.processedAt = Instant.now();
        this.lastError = reason;
    }

    public void markFailed(String error) {
        this.processingStatus = WebhookEventAuditStatus.FAILED;
        this.lastError = error;
    }

    public UUID getId() {
        return id;
    }

    public String getProvider() {
        return provider;
    }

    public String getDeduplicationKey() {
        return deduplicationKey;
    }

    public String getEventType() {
        return eventType;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public String getPayload() {
        return payload;
    }

    public WebhookEventAuditStatus getProcessingStatus() {
        return processingStatus;
    }

    public int getAttempts() {
        return attempts;
    }

    public String getLastError() {
        return lastError;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}