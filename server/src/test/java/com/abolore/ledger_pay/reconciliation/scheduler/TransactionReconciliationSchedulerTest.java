package com.abolore.ledger_pay.reconciliation.scheduler;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import com.abolore.ledger_pay.reconciliation.entity.ReconciliationSchedulerProperties;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationTriggerType;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationSchedulerProperties.Scheduler;
import com.abolore.ledger_pay.reconciliation.service.TransactionReconciliationService;

@ExtendWith(MockitoExtension.class)
class TransactionReconciliationSchedulerTest {

    @Mock
    private TransactionReconciliationService reconciliationService;

    private TransactionReconciliationScheduler scheduler;

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-11-19T17:40:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        Scheduler sc = new Scheduler(true, "0 0 2 * * *", 1);

        ReconciliationSchedulerProperties properties = new ReconciliationSchedulerProperties(sc);

        scheduler = new TransactionReconciliationScheduler(reconciliationService, properties, FIXED_CLOCK);
    }

    @Test
    void shouldExecuteScheduledReconciliation() {

        scheduler.startScheduleRun();

        verify(reconciliationService).reconcile(
                any(Instant.class),
                any(Instant.class),
                eq(ReconciliationTriggerType.SCHEDULED),
                isNull());
    }

    @Test
    void shouldCalculatePreviousDaysUtcDateRange() {

        scheduler.startScheduleRun();

        Instant expectedPeriodStart = Instant.parse("2026-11-18T00:00:00Z");

        Instant expectedPeriodEnd = Instant.parse("2026-11-19T00:00:00Z");

        verify(reconciliationService).reconcile(
                eq(expectedPeriodStart),
                eq(expectedPeriodEnd),
                eq(ReconciliationTriggerType.SCHEDULED),
                isNull());
    }

    @Test
    void shouldNotExecuteWhenSchedulerIsDisabled() {

        Scheduler sc = new Scheduler(false, "0 0 2 * * *", 1);

        ReconciliationSchedulerProperties disabledProperties = new ReconciliationSchedulerProperties(sc);

        scheduler = new TransactionReconciliationScheduler(reconciliationService, disabledProperties, FIXED_CLOCK);

        scheduler.startScheduleRun();

        verifyNoInteractions(reconciliationService);
    }
}