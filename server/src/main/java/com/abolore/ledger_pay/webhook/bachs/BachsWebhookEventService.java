package com.abolore.ledger_pay.webhook.bachs;

import java.util.UUID;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class BachsWebhookEventService {

    private final BachsWebhookEventRepository eventRepository;

    public BachsWebhookEventService(
            BachsWebhookEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    public boolean claimEvent(String eventId, String eventType) {
        int rowsInserted = eventRepository.claimEvent(
                UUID.randomUUID(),
                eventId,
                eventType);

        return rowsInserted == 1;
    }
}