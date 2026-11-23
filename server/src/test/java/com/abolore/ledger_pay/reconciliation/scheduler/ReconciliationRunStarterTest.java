package com.abolore.ledger_pay.reconciliation.scheduler;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import com.abolore.ledger_pay.common.exceptions.DomainException;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationRun;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationRunStatus;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationTriggerType;
import com.abolore.ledger_pay.reconciliation.repository.ReconciliationRunRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReconciliationRunStarterTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ReconciliationRunRepository reconciliationRunRepository;

    private ReconciliationRunStarter starter;

    private final Instant periodStart = Instant.parse("2026-11-18T00:00:00Z");

    private final Instant periodEnd = Instant.parse("2026-11-19T00:00:00Z");

    @BeforeEach
    void setUp() {
        starter = new ReconciliationRunStarter(
                jdbcTemplate,
                reconciliationRunRepository);
    }

    @Test
    void shouldCreateAndStartRunWhenNoOverlappingRunExists() {

        when(reconciliationRunRepository
                .countByProviderAndStatusInAndPeriodOverlap(
                        eq("PAYSTACK"),
                        anyCollection(),
                        eq(periodStart),
                        eq(periodEnd)))
                .thenReturn(0L);

        when(reconciliationRunRepository.saveAndFlush(any(ReconciliationRun.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReconciliationRun result = starter.startRun(
                "PAYSTACK",
                periodStart,
                periodEnd,
                ReconciliationTriggerType.MANUAL,
                "LOCAL_DEV");

        assertNotNull(result);
        assertEquals(ReconciliationRunStatus.RUNNING, result.getStatus());

        verify(reconciliationRunRepository).saveAndFlush(any(ReconciliationRun.class));
    }

    @Test
    void shouldRejectRunWhenOverlappingRunExists() {

        when(reconciliationRunRepository
                .countByProviderAndStatusInAndPeriodOverlap(
                        eq("PAYSTACK"),
                        anyCollection(),
                        eq(periodStart),
                        eq(periodEnd)))
                .thenReturn(1L);

        assertThrows(DomainException.class, () -> starter.startRun(
                "PAYSTACK",
                periodStart,
                periodEnd,
                ReconciliationTriggerType.MANUAL,
                "LOCAL_DEV"));

        verify(reconciliationRunRepository, never())
                .saveAndFlush(any(ReconciliationRun.class));
    }

    @Test
    void shouldAllowRunWhenExistingRunDoesNotOverlap() {

        Instant newPeriodStart = Instant.parse("2026-11-19T00:00:00Z");

        Instant newPeriodEnd = Instant.parse("2026-11-20T00:00:00Z");

        when(reconciliationRunRepository
                .countByProviderAndStatusInAndPeriodOverlap(
                        eq("PAYSTACK"),
                        anyCollection(),
                        eq(newPeriodStart),
                        eq(newPeriodEnd)))
                .thenReturn(0L);

        when(reconciliationRunRepository.saveAndFlush(any(ReconciliationRun.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReconciliationRun result = starter.startRun(
                "PAYSTACK",
                newPeriodStart,
                newPeriodEnd,
                ReconciliationTriggerType.SCHEDULED,
                null);

        assertNotNull(result);
        assertEquals(ReconciliationRunStatus.RUNNING, result.getStatus());

        verify(reconciliationRunRepository).saveAndFlush(any(ReconciliationRun.class));
    }
}