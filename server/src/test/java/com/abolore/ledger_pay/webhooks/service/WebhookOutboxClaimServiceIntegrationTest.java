package com.abolore.ledger_pay.webhooks.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.abolore.ledger_pay.webhooks.entity.WebhookOutbox;
import com.abolore.ledger_pay.webhooks.entity.WebhookOutboxStatus;
import com.abolore.ledger_pay.webhooks.repository.WebhookOutboxRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class WebhookOutboxClaimServiceIntegrationTest {

    @Autowired
    private WebhookOutboxClaimService claimService;

    @Autowired
    private WebhookOutboxRepository webhookOutboxRepository;

    @BeforeEach
    void setUp() {
        webhookOutboxRepository.deleteAll();
    }

    private WebhookOutbox createEvent(String deduplicationKey, WebhookOutboxStatus status, Instant nextAttemptAt,
            Instant leaseUntil) {
        return new WebhookOutbox.Builder()
                .setProvider("PAYSTACK")
                .setEventType("charge.success")
                .setDeduplicationKey(deduplicationKey)
                .setTransactionReference("REF-" + deduplicationKey)
                .setPayload("{}")
                .setStatus(status)
                .setNextAttemptAt(nextAttemptAt)
                .setLeaseUntil(leaseUntil)
                .setAttemptCount(0)
                .build();
    }

    @Test
    void shouldClaimPendingEvent() {

        WebhookOutbox event = createEvent(
                "event-1",
                WebhookOutboxStatus.PENDING,
                Instant.now().minusSeconds(60),
                null);

        webhookOutboxRepository.saveAndFlush(event);

        List<WebhookOutbox> claimedEvents = claimService.claimPendingEvents();

        assertThat(claimedEvents).hasSize(1);
        assertThat(claimedEvents.get(0).getStatus())
                .isEqualTo(WebhookOutboxStatus.PROCESSING);
        assertThat(claimedEvents.get(0).getClaimToken()).isNotNull();
        assertThat(claimedEvents.get(0).getLeaseUntil()).isNotNull();
        assertThat(claimedEvents.get(0).getAttemptCount()).isEqualTo(1);
    }

    @Test
    void shouldNotClaimPendingEventBeforeNextAttemptTime() {

        WebhookOutbox event = createEvent(
                "event-2",
                WebhookOutboxStatus.PENDING,
                Instant.now().plusSeconds(300),
                null);

        webhookOutboxRepository.saveAndFlush(event);

        List<WebhookOutbox> claimedEvents = claimService.claimPendingEvents();

        assertThat(claimedEvents).isEmpty();
    }

    @Test
    void shouldReclaimProcessingEventWithExpiredLease() {

        WebhookOutbox event = createEvent(
                "event-3",
                WebhookOutboxStatus.PROCESSING,
                Instant.now(),
                Instant.now().minusSeconds(60));

        event.claim(UUID.randomUUID(), Instant.now().minusSeconds(60));

        webhookOutboxRepository.saveAndFlush(event);

        List<WebhookOutbox> claimedEvents = claimService.claimPendingEvents();

        assertThat(claimedEvents).hasSize(1);
        assertThat(claimedEvents.get(0).getStatus())
                .isEqualTo(WebhookOutboxStatus.PROCESSING);
        assertThat(claimedEvents.get(0).getAttemptCount()).isEqualTo(2);
    }

    @Test
    void shouldNotClaimProcessingEventWithActiveLease() {

        WebhookOutbox event = createEvent(
                "event-4",
                WebhookOutboxStatus.PROCESSING,
                Instant.now(),
                Instant.now().plusSeconds(120));

        event.claim(UUID.randomUUID(), Instant.now().plusSeconds(120));

        webhookOutboxRepository.saveAndFlush(event);

        List<WebhookOutbox> claimedEvents = claimService.claimPendingEvents();

        assertThat(claimedEvents).isEmpty();
    }
}