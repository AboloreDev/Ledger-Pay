package com.abolore.ledger_pay.reconciliation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abolore.ledger_pay.reconciliation.entity.JournalReconcliationResult;
import com.abolore.ledger_pay.reconciliation.entity.PaymentMatchingResult;
import com.abolore.ledger_pay.reconciliation.entity.PaymentMatchingSummary;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationRecordType;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationResultFactory;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationRun;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationRunStatus;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationTriggerType;
import com.abolore.ledger_pay.reconciliation.entity.TransactionReconciliationResult;
import com.abolore.ledger_pay.reconciliation.repository.ReconciliationResultRepository;
import com.abolore.ledger_pay.reconciliation.repository.ReconciliationRunRepository;

import tools.jackson.core.JacksonException;

@ExtendWith(MockitoExtension.class)
public class ReconciliationResultPersistenceServiceTest {

    @InjectMocks
    private ReconciliationResultPersistenceService persistenceService;

    @Mock
    private ReconciliationResultRepository reconciliationResultRepository;

    @Mock
    private ReconciliationRunRepository reconciliationRunRepository;

    @Mock
    private ReconciliationResultFactory reconciliationResultFactory;

    @Test
    void shouldPersistResultsAndCompleteReconciliationRun() {

        ReconciliationRun run = ReconciliationRun.create(
                "PAYSTACK",
                Instant.parse("2026-11-01T00:00:00Z"),
                Instant.parse("2026-11-02T00:00:00Z"),
                ReconciliationTriggerType.MANUAL,
                "test_user");

        run.start();

        UUID paymentId = UUID.randomUUID();

        PaymentMatchingResult paymentResult = new PaymentMatchingResult(
                ReconciliationRecordType.PAYSTACK_TRANSACTION,
                "PAY-001",
                paymentId,
                false,
                List.of("AMOUNT_MISMATCH"));

        PaymentMatchingSummary summary = new PaymentMatchingSummary(
                1, 1, 0, 1, 0, 0,
                List.of(paymentResult));

        JournalReconcliationResult journalResult = new JournalReconcliationResult(
                paymentId,
                false,
                1,
                List.of("UNBALANCED_JOURNAL"));

        TransactionReconciliationResult result = new TransactionReconciliationResult(
                summary,
                1,
                0,
                1,
                List.of(journalResult));

        persistenceService.persistResultToDB(run, result);

        verify(reconciliationResultRepository).saveAll(anyList());
        verify(reconciliationRunRepository).save(run);

        assertEquals(ReconciliationRunStatus.COMPLETED, run.getStatus());
        assertEquals(1, run.getTotalProviderTransactions());
        assertEquals(1, run.getTotalInternalPayments());
        assertEquals(0, run.getMatchedCount());
        assertEquals(2, run.getDiscrepancyCount());
        assertEquals(1, run.getJournalsChecked());
        assertEquals(0, run.getValidJournals());
        assertEquals(1, run.getInvalidJournals());
    }

    // @Test
    // void shouldNotCompleteRunWhenSavingResultsFails() {

    // ReconciliationRun run = ReconciliationRun.create(
    // "PAYSTACK",
    // Instant.parse("2026-11-01T00:00:00Z"),
    // Instant.parse("2026-11-02T00:00:00Z"));

    // run.start();

    // PaymentMatchingResult result1 = new PaymentMatchingResult(
    // ReconciliationRecordType.PAYSTACK_TRANSACTION,
    // "PAY-001",
    // UUID.randomUUID(),
    // false,
    // List.of("AMOUNT_MISMATCH"));

    // JournalReconcliationResult result2 = new JournalReconcliationResult(
    // UUID.randomUUID(),
    // false,
    // 1,
    // List.of("UNBALANCED_JOURNAL"));

    // TransactionReconciliationResult result = new TransactionReconciliationResult(
    // new PaymentMatchingSummary(1, 1, 0, 0, 0, 0, List.of(result1)),
    // 1, 0, 0,
    // List.of(result2));

    // verify(reconciliationResultFactory, atLeastOnce())
    // .createReconciliationResultThatConvertsJSOBDiscrepancyToString(
    // any(), ReconciliationRecordType.PAYSTACK_TRANSACTION, "PAY-12345",
    // UUID.randomUUID(), false,
    // List.copyOf(result1.discrepancies()));

    // assertThrows(JacksonException.class,
    // () -> persistenceService.persistResultToDB(run, result));

    // assertEquals(ReconciliationRunStatus.RUNNING, run.getStatus());

    // verify(reconciliationRunRepository, never()).save(run);
    // }

    @Test
    void shouldCompleteRunWithZeroDiscrepancies() {

        ReconciliationRun run = ReconciliationRun.create(
                "PAYSTACK",
                Instant.parse("2026-11-01T00:00:00Z"),
                Instant.parse("2026-11-02T00:00:00Z"),
                ReconciliationTriggerType.MANUAL,
                "test-user");

        run.start();

        UUID paymentId = UUID.randomUUID();

        PaymentMatchingResult paymentResult = new PaymentMatchingResult(
                ReconciliationRecordType.PAYSTACK_TRANSACTION,
                "PAY-001",
                paymentId,
                true,
                List.of());

        PaymentMatchingSummary summary = new PaymentMatchingSummary(
                1, 1, 1, 0, 0, 0,
                List.of(paymentResult));

        JournalReconcliationResult journalResult = new JournalReconcliationResult(paymentId, true, 1, List.of());

        TransactionReconciliationResult result = new TransactionReconciliationResult(
                summary, 1, 1, 0, List.of(journalResult));

        persistenceService.persistResultToDB(run, result);

        assertEquals(ReconciliationRunStatus.COMPLETED, run.getStatus());
        assertEquals(1, run.getMatchedCount());
        assertEquals(0, run.getDiscrepancyCount());
        assertEquals(1, run.getJournalsChecked());
        assertEquals(1, run.getValidJournals());
        assertEquals(0, run.getInvalidJournals());

        // verify(reconciliationResultFactory)(anyList());
        verify(reconciliationRunRepository).save(run);
    }
}
