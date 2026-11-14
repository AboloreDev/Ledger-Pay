package com.abolore.ledger_pay.webhooks.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.SendResult;

import com.abolore.ledger_pay.webhooks.entity.WebhookOutbox;
import com.abolore.ledger_pay.webhooks.kafka.WebhookKafkaProducer;

class WebhookOutboxPublisherTest {

        private static final String DEDUP_KEY = "PAYSTACK-charge-success-123";
        private static final String PAYLOAD = "{\"event\":\"charge.success\"}";

        private WebhookOutboxClaimService webhookOutboxClaimService;
        private WebhookKafkaProducer webhookKafkaProducer;
        private WebhookOutboxPublisher webhookOutboxPublisher;

        private final UUID eventId = UUID.randomUUID();
        private final UUID claimToken = UUID.randomUUID();

        @BeforeEach
        void setUp() {
                webhookOutboxClaimService = mock(WebhookOutboxClaimService.class);
                webhookKafkaProducer = mock(WebhookKafkaProducer.class);

                webhookOutboxPublisher = new WebhookOutboxPublisher(
                                webhookOutboxClaimService,
                                webhookKafkaProducer);
        }

        @Test
        void shouldPublishEventAndMarkItAsPublished() {
                WebhookOutbox event = claimedEvent();

                when(webhookOutboxClaimService.claimPendingEvents())
                                .thenReturn(List.of(event));

                @SuppressWarnings("unchecked")
                SendResult<String, String> sendResult = mock(SendResult.class);

                when(webhookKafkaProducer.sendMessageToConsumer(DEDUP_KEY, PAYLOAD))
                                .thenReturn(CompletableFuture.completedFuture(sendResult));

                webhookOutboxPublisher.publishPendingEvents();

                verify(webhookKafkaProducer).sendMessageToConsumer(DEDUP_KEY, PAYLOAD);
                verify(webhookOutboxClaimService).markPublished(eventId, claimToken);
                verify(webhookOutboxClaimService, never()).markFailed(any(), any(), any());
        }

        @Test
        void shouldMarkEventAsFailedWhenKafkaPublishingFails() {
                WebhookOutbox event = claimedEvent();

                when(webhookOutboxClaimService.claimPendingEvents())
                                .thenReturn(List.of(event));

                CompletableFuture<SendResult<String, String>> failedFuture = new CompletableFuture<>();
                failedFuture.completeExceptionally(new RuntimeException("Kafka unavailable"));

                when(webhookKafkaProducer.sendMessageToConsumer(DEDUP_KEY, PAYLOAD))
                                .thenReturn(failedFuture);

                webhookOutboxPublisher.publishPendingEvents();

                verify(webhookOutboxClaimService)
                                .markFailed(eq(eventId), eq(claimToken), contains("Kafka unavailable"));
                verify(webhookOutboxClaimService, never()).markPublished(any(), any());
        }

        @Test
        void shouldDoNothingWhenNoEventsAreClaimed() {
                when(webhookOutboxClaimService.claimPendingEvents())
                                .thenReturn(List.of());

                webhookOutboxPublisher.publishPendingEvents();

                verify(webhookKafkaProducer, never()).sendMessageToConsumer(any(), any());
                verify(webhookOutboxClaimService, never()).markPublished(any(), any());
                verify(webhookOutboxClaimService, never()).markFailed(any(), any(), any());
        }

        private WebhookOutbox claimedEvent() {
                WebhookOutbox event = mock(WebhookOutbox.class);

                when(event.getId()).thenReturn(eventId);
                when(event.getClaimToken()).thenReturn(claimToken);
                when(event.getDeduplicationKey()).thenReturn(DEDUP_KEY);
                when(event.getPayload()).thenReturn(PAYLOAD);

                return event;
        }
}