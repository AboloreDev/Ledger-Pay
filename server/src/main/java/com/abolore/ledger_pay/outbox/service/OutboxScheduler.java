package com.abolore.ledger_pay.outbox.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxScheduler.class);

    private final OutboxEventProcessingService processingService;

    public OutboxScheduler(OutboxEventProcessingService processingService) {
        this.processingService = processingService;
    }

    @Scheduled(fixedDelayString = "${outbox.poll-interval-ms:1000}")
    public void processOutbox() {
        try {
            processingService.processNextEvent();
        } catch (Exception exception) {
            log.error("Unexpected error while processing outbox", exception);
        }
    }
}
