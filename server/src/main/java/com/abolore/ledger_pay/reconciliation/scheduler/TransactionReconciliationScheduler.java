package com.abolore.ledger_pay.reconciliation.scheduler;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.abolore.ledger_pay.reconciliation.entity.ReconciliationSchedulerProperties;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationTriggerType;
import com.abolore.ledger_pay.reconciliation.service.TransactionReconciliationService;

@Component
public class TransactionReconciliationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TransactionReconciliationScheduler.class);

    private final TransactionReconciliationService transactionReconciliationService;
    private final ReconciliationSchedulerProperties reconciliationSchedulerProperties;
    private final Clock clock;

    public TransactionReconciliationScheduler(TransactionReconciliationService transactionReconciliationService,
            ReconciliationSchedulerProperties reconciliationSchedulerProperties, Clock clock) {
        this.transactionReconciliationService = transactionReconciliationService;
        this.reconciliationSchedulerProperties = reconciliationSchedulerProperties;
        this.clock = clock;
    }

    @Scheduled(cron = "${reconciliation.scheduler.cron}")
    public void startScheduleRun() {

        if (reconciliationSchedulerProperties.scheduler().isEnabled() != true) {
            logger.info("Transaction Reconciliation Scheduler is disabled. Skipping execution.");
            return;
        }

        Instant periodEnd = LocalDate.now(clock).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant periodStart = periodEnd.minus(reconciliationSchedulerProperties.scheduler().lookBackDays(),
                ChronoUnit.DAYS);

        try {
            logger.info("Starting scheduled reconciliation. Period: {} to {}",
                    periodStart, periodEnd);

            transactionReconciliationService.reconcile(periodStart, periodEnd, ReconciliationTriggerType.SCHEDULED,
                    null);

            logger.info(
                    "Scheduled reconciliation completed successfully. Period: {} to {}",
                    periodStart, periodEnd);

        } catch (Exception exception) {
            logger.error("Scheduled reconciliation failed.", exception);
        }
    }
}
