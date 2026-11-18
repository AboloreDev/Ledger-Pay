package com.abolore.ledger_pay.reconciliation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;

import com.abolore.ledger_pay.payment.entity.Payment;
import com.abolore.ledger_pay.reconciliation.entity.JournalReconcliationResult;
import com.abolore.ledger_pay.reconciliation.entity.PaymentMatchingResult;
import com.abolore.ledger_pay.reconciliation.entity.PaymentMatchingSummary;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationRecordType;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationRun;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationRunStatus;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationTriggerType;
import com.abolore.ledger_pay.reconciliation.entity.TransactionReconciliationResult;
import com.abolore.ledger_pay.reconciliation.repository.ReconciliationRunRepository;

@ExtendWith(MockitoExtension.class)
class TransactionReconciliationServiceTest {

        @Mock
        private PaymentMatchingService paymentMatchingService;

        @Mock
        private InternalPaymentFetchService internalPaymentFetchService;

        @Mock
        private JournalReconciliationService journalReconciliationService;

        @InjectMocks
        private TransactionReconciliationService transactionReconciliationService;

        @Mock
        private ReconciliationResultPersistenceService persistenceService;

        @Mock
        private ReconciliationRunRepository runRepository;

        @Test
        void shouldReconcilePaymentAndValidJournal() {

                Instant start = Instant.parse("2026-11-01T00:00:00Z");
                Instant end = Instant.parse("2026-11-02T00:00:00Z");
                ReconciliationTriggerType triggerType = ReconciliationTriggerType.MANUAL;
                String initiatedBy = "TEST_USER";

                UUID paymentId = UUID.randomUUID();
                BigDecimal amount = new BigDecimal(5000);

                PaymentMatchingResult matchingResult = new PaymentMatchingResult(
                                ReconciliationRecordType.PAYSTACK_TRANSACTION,
                                "PAY-001",
                                paymentId,
                                true,
                                List.of());

                PaymentMatchingSummary matchingSummary = new PaymentMatchingSummary(1, 1, 1, 0, 0, 0,
                                List.of(matchingResult));

                Payment payment = new Payment.Builder().setId(paymentId).setReference("PAY-001")
                                .setAmount(amount)
                                .setCurrency("NGN")
                                .build();

                JournalReconcliationResult journalResult = new JournalReconcliationResult(paymentId, true, 1,
                                List.of());

                when(paymentMatchingService.match(start, end))
                                .thenReturn(matchingSummary);

                when(internalPaymentFetchService.fetchPaymentsByIds(List.of(paymentId)))
                                .thenReturn(List.of(payment));

                when(journalReconciliationService.reconcileResults(List.of(payment)))
                                .thenReturn(Map.of(paymentId, journalResult));

                TransactionReconciliationResult result = transactionReconciliationService.reconcile(start, end,
                                triggerType, initiatedBy);

                assertEquals(1, result.journalCheckedCount());
                assertEquals(1, result.validJournalCount());
                assertEquals(0, result.invalidJournalCount());
                assertEquals(1, result.journalResults().size());

                verify(paymentMatchingService).match(start, end);
                verify(internalPaymentFetchService).fetchPaymentsByIds(List.of(paymentId));
                verify(journalReconciliationService).reconcileResults(List.of(payment));
        }

        @Test
        void shouldReportInvalidJournal() {

                Instant start = Instant.parse("2026-11-01T00:00:00Z");
                Instant end = Instant.parse("2026-11-02T00:00:00Z");
                ReconciliationTriggerType triggerType = ReconciliationTriggerType.MANUAL;
                String initiatedBy = "TEST_USER";

                UUID paymentId = UUID.randomUUID();
                BigDecimal amount = new BigDecimal(5000);

                PaymentMatchingResult matchingResult = new PaymentMatchingResult(
                                ReconciliationRecordType.PAYSTACK_TRANSACTION,
                                "PAY-001",
                                paymentId,
                                true,
                                List.of());

                PaymentMatchingSummary matchingSummary = new PaymentMatchingSummary(
                                1, 1, 1, 0, 0, 0,
                                List.of(matchingResult));

                Payment payment = new Payment.Builder().setId(paymentId).setReference("PAY-001")
                                .setAmount(amount)
                                .setCurrency("NGN")
                                .build();

                JournalReconcliationResult journalResult = new JournalReconcliationResult(
                                paymentId,
                                false,
                                1,
                                List.of("UNBALANCED_JOURNAL"));

                when(paymentMatchingService.match(start, end))
                                .thenReturn(matchingSummary);

                when(internalPaymentFetchService.fetchPaymentsByIds(List.of(paymentId)))
                                .thenReturn(List.of(payment));

                when(journalReconciliationService.reconcileResults(List.of(payment)))
                                .thenReturn(Map.of(paymentId, journalResult));

                TransactionReconciliationResult result = transactionReconciliationService.reconcile(start, end,
                                triggerType, initiatedBy);

                assertEquals(1, result.journalCheckedCount());
                assertEquals(0, result.validJournalCount());
                assertEquals(1, result.invalidJournalCount());

                assertFalse(result.journalResults().get(0).valid());
                assertTrue(result.journalResults().get(0).discrepancies()
                                .contains("UNBALANCED_JOURNAL"));
        }

        @Test
        void shouldReconcileJournalEvenWhenPaymentHasDiscrepancies() {

                Instant start = Instant.parse("2026-11-01T00:00:00Z");
                Instant end = Instant.parse("2026-11-02T00:00:00Z");
                ReconciliationTriggerType triggerType = ReconciliationTriggerType.MANUAL;
                String initiatedBy = "TEST_USER";

                UUID paymentId = UUID.randomUUID();
                BigDecimal amount = new BigDecimal(5000);

                PaymentMatchingResult matchingResult = new PaymentMatchingResult(
                                ReconciliationRecordType.PAYSTACK_TRANSACTION,
                                "PAY-001",
                                paymentId,
                                false,
                                List.of("AMOUNT_MISMATCH"));

                PaymentMatchingSummary matchingSummary = new PaymentMatchingSummary(
                                1, 1, 0, 1, 0, 0,
                                List.of(matchingResult));

                Payment payment = new Payment.Builder().setId(paymentId).setReference("PAY-001")
                                .setAmount(amount)
                                .setCurrency("NGN")
                                .build();

                JournalReconcliationResult journalResult = new JournalReconcliationResult(paymentId, true, 1,
                                List.of());

                when(paymentMatchingService.match(start, end))
                                .thenReturn(matchingSummary);

                when(internalPaymentFetchService.fetchPaymentsByIds(List.of(paymentId)))
                                .thenReturn(List.of(payment));

                when(journalReconciliationService.reconcileResults(List.of(payment)))
                                .thenReturn(Map.of(paymentId, journalResult));

                TransactionReconciliationResult result = transactionReconciliationService.reconcile(start, end,
                                triggerType, initiatedBy);

                assertEquals(1, result.paymentMatchingSummary().discrepantCount());
                assertEquals(1, result.journalCheckedCount());
                assertEquals(1, result.validJournalCount());

                verify(journalReconciliationService).reconcileResults(List.of(payment));
        }

        @Test
        void shouldSkipJournalReconciliationWhenNoInternalPaymentExists() {

                Instant start = Instant.parse("2026-11-01T00:00:00Z");
                Instant end = Instant.parse("2026-11-02T00:00:00Z");
                ReconciliationTriggerType triggerType = ReconciliationTriggerType.MANUAL;
                String initiatedBy = "TEST_USER";

                PaymentMatchingResult matchingResult = new PaymentMatchingResult(
                                ReconciliationRecordType.PAYSTACK_TRANSACTION,
                                "PAY-001",
                                null,
                                false,
                                List.of("MISSING_INTERNAL_PAYMENT"));

                PaymentMatchingSummary matchingSummary = new PaymentMatchingSummary(
                                1, 0, 0, 0, 1, 0,
                                List.of(matchingResult));

                when(paymentMatchingService.match(start, end))
                                .thenReturn(matchingSummary);

                when(internalPaymentFetchService.fetchPaymentsByIds(List.of()))
                                .thenReturn(List.of());

                when(journalReconciliationService.reconcileResults(List.of()))
                                .thenReturn(Map.of());

                TransactionReconciliationResult result = transactionReconciliationService.reconcile(start, end,
                                triggerType, initiatedBy);

                assertEquals(1, result.paymentMatchingSummary().unmatchedPaystackCount());
                assertEquals(0, result.journalCheckedCount());
                assertEquals(0, result.validJournalCount());
                assertEquals(0, result.invalidJournalCount());

                verify(journalReconciliationService).reconcileResults(List.of());
        }

        @Test
        void shouldCompleteReconciliationRunSuccessfully() {

                Instant start = Instant.parse("2026-11-01T00:00:00Z");
                Instant end = Instant.parse("2026-11-02T00:00:00Z");
                ReconciliationTriggerType triggerType = ReconciliationTriggerType.MANUAL;
                String initiatedBy = "TEST_USER";

                UUID paymentId = UUID.randomUUID();
                BigDecimal amount = new BigDecimal(5000);

                PaymentMatchingResult matchingResult = new PaymentMatchingResult(
                                ReconciliationRecordType.PAYSTACK_TRANSACTION,
                                "PAY-001",
                                paymentId,
                                true,
                                List.of());

                PaymentMatchingSummary matchingSummary = new PaymentMatchingSummary(
                                1, 1, 1, 0, 0, 0,
                                List.of(matchingResult));

                Payment payment = new Payment.Builder().setId(paymentId).setReference("PAY-001")
                                .setAmount(amount)
                                .setCurrency("NGN")
                                .build();

                JournalReconcliationResult journalResult = new JournalReconcliationResult(paymentId, true, 1,
                                List.of());

                when(paymentMatchingService.match(start, end))
                                .thenReturn(matchingSummary);

                when(internalPaymentFetchService.fetchPaymentsByIds(List.of(paymentId)))
                                .thenReturn(List.of(payment));

                when(journalReconciliationService.reconcileResults(List.of(payment)))
                                .thenReturn(Map.of(paymentId, journalResult));

                TransactionReconciliationResult result = transactionReconciliationService.reconcile(start, end,
                                triggerType, initiatedBy);

                assertNotNull(result);
                assertEquals(1, result.journalCheckedCount());
                assertEquals(1, result.validJournalCount());
                assertEquals(0, result.invalidJournalCount());

                ArgumentCaptor<ReconciliationRun> captor = ArgumentCaptor.forClass(ReconciliationRun.class);

                verify(runRepository, times(2)).save(captor.capture());

                ReconciliationRun savedRun = captor.getValue();

                assertEquals(ReconciliationRunStatus.RUNNING, savedRun.getStatus());
                verify(persistenceService).persistResultToDB(any(ReconciliationRun.class), eq(result));
        }

        @Test
        void shouldCompleteRunWhenPaymentDiscrepanciesExist() {

                Instant start = Instant.parse("2026-11-01T00:00:00Z");
                Instant end = Instant.parse("2026-11-02T00:00:00Z");
                ReconciliationTriggerType triggerType = ReconciliationTriggerType.MANUAL;
                String initiatedBy = "TEST_USER";

                UUID paymentId = UUID.randomUUID();
                BigDecimal amount = new BigDecimal(5000);

                PaymentMatchingResult matchingResult = new PaymentMatchingResult(
                                ReconciliationRecordType.PAYSTACK_TRANSACTION,
                                "PAY-001",
                                paymentId,
                                false,
                                List.of("AMOUNT_MISMATCH"));

                PaymentMatchingSummary summary = new PaymentMatchingSummary(
                                1, 1, 0, 1, 0, 0,
                                List.of(matchingResult));

                Payment payment = new Payment.Builder().setId(paymentId).setReference("PAY-001")
                                .setAmount(amount)
                                .setCurrency("NGN")
                                .build();

                JournalReconcliationResult journalResult = new JournalReconcliationResult(paymentId, true, 1,
                                List.of());

                when(paymentMatchingService.match(start, end))
                                .thenReturn(summary);

                when(internalPaymentFetchService.fetchPaymentsByIds(List.of(paymentId)))
                                .thenReturn(List.of(payment));

                when(journalReconciliationService.reconcileResults(List.of(payment)))
                                .thenReturn(Map.of(paymentId, journalResult));

                TransactionReconciliationResult result = transactionReconciliationService.reconcile(start, end,
                                triggerType, initiatedBy);

                assertEquals(1, result.paymentMatchingSummary().discrepantCount());
                assertEquals(1, result.journalCheckedCount());

                verify(persistenceService).persistResultToDB(
                                any(ReconciliationRun.class), eq(result));
        }

        @Test
        void shouldMarkRunAsFailedWhenReconciliationThrowsException() {

                Instant start = Instant.parse("2026-11-01T00:00:00Z");
                Instant end = Instant.parse("2026-11-02T00:00:00Z");
                ReconciliationTriggerType triggerType = ReconciliationTriggerType.MANUAL;
                String initiatedBy = "TEST_USER";

                RuntimeException exception = new RuntimeException("Paystack API unavailable");

                when(paymentMatchingService.match(start, end))
                                .thenThrow(exception);

                assertThrows(RuntimeException.class,
                                () -> transactionReconciliationService.reconcile(start, end, triggerType, initiatedBy));

                ArgumentCaptor<ReconciliationRun> captor = ArgumentCaptor.forClass(ReconciliationRun.class);

                verify(runRepository, times(3)).save(captor.capture());

                ReconciliationRun failedRun = captor.getValue();

                assertEquals(ReconciliationRunStatus.FAILED, failedRun.getStatus());
                assertEquals("Paystack API unavailable", failedRun.getFailureReason());

                verify(persistenceService, never())
                                .persistResultToDB(any(), any());
        }
}
