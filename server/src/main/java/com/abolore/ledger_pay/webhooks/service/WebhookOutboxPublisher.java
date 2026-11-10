package com.abolore.ledger_pay.webhooks.service;

import java.util.List;
import java.util.UUID;
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

            UUID eventId = event.getId();
            UUID claimToken = event.getClaimToken();

            // Publish to Kafka
            try {
                webhookKafkaProducer
                        .sendMessageToConsumer(
                                event.getDeduplicationKey(),
                                event.getPayload())
                        .join();

            } catch (RuntimeException exception) {

                Throwable cause = (exception instanceof CompletionException
                        && exception.getCause() != null)
                                ? exception.getCause()
                                : exception;

                // Kafka publishing failed
                try {
                    webhookOutboxClaimService.markFailed(
                            eventId,
                            claimToken,
                            cause.getMessage());

                } catch (RuntimeException databaseException) {
                    logger.error(
                            "Kafka publishing failed, and outbox failure status could not be saved. eventId={}",
                            eventId,
                            databaseException);
                }

                failed++;

                logger.error("Failed to publish outbox event id={} dedupKey={}", eventId, event.getDeduplicationKey(),
                        cause);

                continue;
            }

            // Kafka publishing succeeded
            try {
                webhookOutboxClaimService.markPublished(eventId, claimToken);

                published++;

                logger.info("Published outbox event id={} dedupKey={}", eventId, event.getDeduplicationKey());

            } catch (RuntimeException databaseException) {

                // Kafka succeeded, but database update failed
                logger.error(
                        "Kafka accepted the event, but outbox status could not be updated to PUBLISHED. eventId={} dedupKey={}. Event may be published again after lease expiry.",
                        eventId,
                        event.getDeduplicationKey(),
                        databaseException);
            }
        }

        logger.info("Outbox publish run finished: published={}, failed={}", published, failed);
    }
}