package com.abolore.ledger_pay.webhooks.kafka;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
public class WebhookKafkaProducer {

    private static final Logger logger = LoggerFactory.getLogger(WebhookKafkaProducer.class);

    private static final String TOPIC = "webhook-events";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public WebhookKafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<SendResult<String, String>> sendMessageToConsumer(String eventKey, String payload) {

        logger.info("Sending message to topic {} with key {}", TOPIC, eventKey);

        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC, eventKey, payload);

        return future.whenComplete((result, exception) -> {

            if (exception != null) {
                logger.error("Failed to send message with ID: {}", eventKey, exception);
            } else {
                logger.info(
                        "Message {} sent to partition {} at offset {}",
                        eventKey,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
