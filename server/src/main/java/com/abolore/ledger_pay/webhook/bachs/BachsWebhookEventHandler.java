package com.abolore.ledger_pay.webhook.bachs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.abolore.ledger_pay.payment.entity.Payment;
import com.abolore.ledger_pay.payment.service.PaymentService;

@Service
public class BachsWebhookEventHandler {

    private static final Logger log = LoggerFactory.getLogger(BachsWebhookEventHandler.class);
    private final PaymentService paymentService;

    public BachsWebhookEventHandler(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public void handle(BachsWebhookEvent event) {
        switch (event.type()) {
            case "collection.succeeded" ->
                handleCollectionSucceeded(event);

            case "collection.failed" ->
                handleCollectionFailed(event);

            default ->
                log.info(
                        "Ignoring unsupported Bachs event type: {}",
                        event.type());
        }
    }

    private void handleCollectionSucceeded(BachsWebhookEvent event) {
        String reference = event.data().path("reference").asText("");

        if (reference.isBlank()) {
            throw new IllegalArgumentException(
                    "Bachs event is missing payment reference");
        }

        Payment payment = paymentService.getPaymentByReference(reference);

        paymentService.markPaymentSucceeded(payment.getId());

        log.info("Processed successful collection for reference: {}", reference);
    }

    private void handleCollectionFailed(BachsWebhookEvent event) {
        String reference = event.data().path("reference").asText("");

        if (reference.isBlank()) {
            throw new IllegalArgumentException(
                    "Bachs event is missing payment reference");
        }

        Payment payment = paymentService.getPaymentByReference(reference);

        paymentService.markPaymentFailed(payment.getId());

        log.info("Processed failure collection for reference: {}", reference);
    }
}