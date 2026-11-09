package com.abolore.ledger_pay.webhooks.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.abolore.ledger_pay.payment.provider.service.PaystackVerificationService;
import com.abolore.ledger_pay.webhook_event_audit.entity.WebhookEventAudit;
import com.abolore.ledger_pay.webhook_event_audit.service.WebhookEventAuditService;
import com.abolore.ledger_pay.webhooks.dto.PaystackWebhookEvent;

import tools.jackson.databind.ObjectMapper;

@Service
public class WebhookKafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(WebhookKafkaConsumer.class);

    private static final String PROVIDER = "PAYSTACK";

    private final ObjectMapper objectMapper;
    private final PaystackVerificationService paystackVerificationService;
    private final WebhookEventAuditService auditService;

    public WebhookKafkaConsumer(ObjectMapper objectMapper, PaystackVerificationService paystackVerificationService,
            WebhookEventAuditService auditService) {
        this.objectMapper = objectMapper;
        this.paystackVerificationService = paystackVerificationService;
        this.auditService = auditService;
    }

    @KafkaListener(topics = "webhook-events", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeWebhookEvent(ConsumerRecord<String, String> record) {

        logger.info("Received webhook event key={} topic={} partition={} offset={}",
                record.key(), record.topic(), record.partition(), record.offset());

        String deduplicationKey = record.key();

        try {
            auditService.markProcessing(PROVIDER, deduplicationKey);

            PaystackWebhookEvent event = objectMapper.readValue(
                    record.value(),
                    PaystackWebhookEvent.class);

            if (!"charge.success".equals(event.event())) {
                logger.info("Ignoring unsupported event: {}", event.event());
                return;
            }

            String reference = event.data().reference();

            var transaction = paystackVerificationService.verifyTransaction(reference);

            logger.info(
                    "Paystack transaction verified. Reference: {}, Amount: {}, Currency: {}",
                    transaction.reference(),
                    transaction.amount(),
                    transaction.currency());

        } catch (RuntimeException ex) {
            logger.error("Failed to process webhook event key={} partition={} offset={}",
                    record.key(), record.partition(), record.offset(), ex);
            throw ex;
        }
    }
}