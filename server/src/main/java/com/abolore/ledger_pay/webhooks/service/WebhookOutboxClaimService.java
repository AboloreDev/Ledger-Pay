package com.abolore.ledger_pay.webhooks.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.abolore.ledger_pay.common.exceptions.DomainException;
import com.abolore.ledger_pay.common.exceptions.ErrorCode;
import com.abolore.ledger_pay.webhooks.entity.WebhookOutbox;
import com.abolore.ledger_pay.webhooks.repository.WebhookOutboxRepository;
import com.abolore.ledger_pay.webhooks.security.RetryBackoffPolicy;

import jakarta.transaction.Transactional;

@Service
public class WebhookOutboxClaimService {

    private final WebhookOutboxRepository webhookOutboxRepository;
    private final RetryBackoffPolicy retryBackoffPolicy;

    private static final int BATCH_SIZE = 50;
    private static final long LEASE_DURATION_MINUTES = 2;

    public WebhookOutboxClaimService(WebhookOutboxRepository webhookOutboxRepository,
            RetryBackoffPolicy retryBackoffPolicy) {
        this.webhookOutboxRepository = webhookOutboxRepository;
        this.retryBackoffPolicy = retryBackoffPolicy;
    }

    @Transactional
    public List<WebhookOutbox> claimPendingEvents() {

        Instant now = Instant.now();

        List<WebhookOutbox> events = webhookOutboxRepository.findEventsToClaim(now, BATCH_SIZE);

        for (WebhookOutbox event : events) {
            event.claim(UUID.randomUUID(), now.plus(LEASE_DURATION_MINUTES, ChronoUnit.MINUTES));
        }

        return events;
    }

    @Transactional
    public void markPublished(UUID eventId, UUID claimToken) {

        WebhookOutbox event = webhookOutboxRepository.findById(eventId)
                .orElseThrow(() -> DomainException.of(ErrorCode.WEBHOOK_EVENT_NOT_FOUND,
                        "Webhook outbox event not found: " + eventId));

        event.markPublished(claimToken);
    }

    @Transactional
    public void markFailed(UUID eventId, UUID claimToken, String error) {

        WebhookOutbox event = webhookOutboxRepository.findById(eventId)
                .orElseThrow(() -> DomainException.of(ErrorCode.WEBHOOK_EVENT_NOT_FOUND,
                        "Webhook outbox event not found: " + eventId));

        event.markFailed(claimToken, error, retryBackoffPolicy);
    }
}