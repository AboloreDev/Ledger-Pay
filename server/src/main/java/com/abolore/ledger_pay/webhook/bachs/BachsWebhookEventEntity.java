package com.abolore.ledger_pay.webhook.bachs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bachs_webhook_events", uniqueConstraints = {
        @UniqueConstraint(name = "uq_bachs_webhook_provider_event", columnNames = "provider_event_id")
})
public class BachsWebhookEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "provider_event_id", nullable = false, length = 150)
    private String providerEventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BachsWebhookEventStatus status;

    protected BachsWebhookEventEntity() {
    }

    public UUID getId() {
        return id;
    }

    public String getProviderEventId() {
        return providerEventId;
    }

    public String getEventType() {
        return eventType;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public BachsWebhookEventStatus getStatus() {
        return status;
    }
}