package com.abolore.ledger_pay.webhooks.service;

import java.util.List;
import java.util.concurrent.CompletionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.abolore.ledger_pay.webhooks.entity.WebhookOutbox;
import com.abolore.ledger_pay.webhooks.kafka.WebhookKafkaProducer;

@Service
public class WebhookOutboxPublisher {

    private static final Logger logger = LoggerFactory.getLogger(WebhookKafkaProducer.class);
    private final WebhookOutboxClaimService webhookOutboxClaimService;
    private final WebhookKafkaProducer webhookKafkaProducer;

    public WebhookOutboxPublisher(
            WebhookOutboxClaimService webhookOutboxClaimService,
            WebhookKafkaProducer webhookKafkaProducer) {
        this.webhookOutboxClaimService = webhookOutboxClaimService;
        this.webhookKafkaProducer = webhookKafkaProducer;
    }

    @Scheduled(fixedDelayString = "${outbox.poll-interval-ms:1000}")
    public void publishPendingEvents() {

        List<WebhookOutbox> events = webhookOutboxClaimService.claimPendingEvents();

        if (events.isEmpty()) {
            logger.debug("No outbox events to publish");
            return;
        }

        logger.info("Publishing {} outbox event(s) to Kafka", events.size());

        int published = 0;
        int failed = 0;

        for (WebhookOutbox event : events) {
            try {
                webhookKafkaProducer
                        .sendMessageToConsumer(event.getDeduplicationKey(), event.getPayload())
                        .join();

                event.markPublished(event.getId());

                published++;

                logger.info("Published outbox event id={} dedupKey={}",
                        event.getId(), event.getDeduplicationKey());

            } catch (RuntimeException exception) {
                Throwable cause = (exception instanceof CompletionException && exception.getCause() != null)
                        ? exception.getCause()
                        : exception;

                event.markFailed(event.getId(), cause.getMessage());
                failed++;

                logger.error("Failed to publish outbox event id={} dedupKey={}",
                        event.getId(), event.getDeduplicationKey(), cause);
            }
        }

        logger.info("Outbox publish run finished: published={}, failed={}", published, failed);

    }
}