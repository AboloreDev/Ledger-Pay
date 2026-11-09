package com.abolore.ledger_pay.webhooks.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.SendResult;

import com.abolore.ledger_pay.webhooks.entity.WebhookOutbox;
import com.abolore.ledger_pay.webhooks.kafka.WebhookKafkaProducer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

class WebhookOutboxPublisherTest {

    private WebhookOutboxClaimService webhookOutboxClaimService;
    private WebhookKafkaProducer webhookKafkaProducer;
    private WebhookOutboxPublisher webhookOutboxPublisher;

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

        UUID eventId = UUID.randomUUID();

        WebhookOutbox event = mock(WebhookOutbox.class);

        when(event.getId()).thenReturn(eventId);
        when(event.getDeduplicationKey()).thenReturn("PAYSTACK-charge-success-123");
        when(event.getPayload()).thenReturn("{\"event\":\"charge.success\"}");

        when(webhookOutboxClaimService.claimPendingEvents())
                .thenReturn(List.of(event));

        when(webhookKafkaProducer.sendMessageToConsumer(
                "PAYSTACK-charge-success-123",
                "{\"event\":\"charge.success\"}")).thenReturn(CompletableFuture.completedFuture(null));

        webhookOutboxPublisher.publishPendingEvents();

        verify(webhookKafkaProducer).sendMessageToConsumer(
                "PAYSTACK-charge-success-123",
                "{\"event\":\"charge.success\"}");

        verify(event).markPublished(eventId);

        verify(event, never()).markFailed(any(), any());
    }

    @Test
    void shouldMarkEventAsFailedWhenKafkaPublishingFails() {

        UUID eventId = UUID.randomUUID();

        WebhookOutbox event = mock(WebhookOutbox.class);

        when(event.getId()).thenReturn(eventId);
        when(event.getDeduplicationKey()).thenReturn("PAYSTACK-charge-success-123");
        when(event.getPayload()).thenReturn("{\"event\":\"charge.success\"}");

        when(webhookOutboxClaimService.claimPendingEvents())
                .thenReturn(List.of(event));

        CompletableFuture<SendResult<String, String>> failedFuture = new CompletableFuture<>();

        failedFuture.completeExceptionally(
                new RuntimeException("Kafka unavailable"));

        when(webhookKafkaProducer.sendMessageToConsumer(
                "PAYSTACK-charge-success-123",
                "{\"event\":\"charge.success\"}")).thenReturn(failedFuture);

        webhookOutboxPublisher.publishPendingEvents();

        verify(event).markFailed(
                eq(eventId),
                contains("Kafka unavailable"));

        verify(event, never()).markPublished(any());
    }
}