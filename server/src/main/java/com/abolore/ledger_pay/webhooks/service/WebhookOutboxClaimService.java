package com.abolore.ledger_pay.webhooks.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.abolore.ledger_pay.webhooks.entity.WebhookOutbox;
import com.abolore.ledger_pay.webhooks.repository.WebhookOutboxRepository;

import jakarta.transaction.Transactional;

@Service
public class WebhookOutboxClaimService {

    private final WebhookOutboxRepository webhookOutboxRepository;

    private static final int BATCH_SIZE = 50;
    private static final long LEASE_DURATION_MINUTES = 2;

    public WebhookOutboxClaimService(WebhookOutboxRepository webhookOutboxRepository) {
        this.webhookOutboxRepository = webhookOutboxRepository;
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
}