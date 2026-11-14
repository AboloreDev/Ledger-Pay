package com.abolore.ledger_pay.reconciliation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abolore.ledger_pay.journal.entity.JournalEntries;
import com.abolore.ledger_pay.journal.entity.JournalLines;
import com.abolore.ledger_pay.journal.entity.JournalSides;
import com.abolore.ledger_pay.journal.repository.JournalEntryRepository;
import com.abolore.ledger_pay.journal.repository.JournalLineRepository;
import com.abolore.ledger_pay.ledger.entity.LedgerAccount;
import com.abolore.ledger_pay.payment.entity.Payment;
import com.abolore.ledger_pay.payment.entity.PaymentStatus;
import com.abolore.ledger_pay.reconciliation.entity.JournalReconcliationResult;

@ExtendWith(MockitoExtension.class)
class JournalReconciliationServiceTest {

        private static final String CURRENCY = "NGN";

        @Mock
        private JournalEntryRepository journalEntryRepository;

        @Mock
        private JournalLineRepository journalLineRepository;

        private JournalReconciliationService service;

        private final UUID providerClearingId = UUID.randomUUID();
        private final UUID merchantPayableId = UUID.randomUUID();
        private final UUID paymentId = UUID.randomUUID();
        private final BigDecimal paymentAmount = new BigDecimal("5000.00");

        @BeforeEach
        void setUp() {
                service = new JournalReconciliationService(
                                journalEntryRepository,
                                journalLineRepository,
                                providerClearingId,
                                merchantPayableId);
        }

        // Valid balanced journal
        @Test
        void shouldReconcileValidBalancedJournal() {
                Payment payment = successfulPayment();
                JournalEntries entry = journalEntry(paymentId);

                List<JournalLines> lines = List.of(
                                journalLine(entry, providerClearingId, JournalSides.DEBIT, paymentAmount, CURRENCY),
                                journalLine(entry, merchantPayableId, JournalSides.CREDIT, paymentAmount, CURRENCY));

                mockJournalData(List.of(entry), lines);

                JournalReconcliationResult result = reconcile(payment);

                assertTrue(result.valid());
                assertTrue(result.discrepancies().isEmpty());
                assertEquals(1, result.journalEntryCount());
        }

        // Missing journal
        @Test
        void shouldDetectMissingJournal() {
                Payment payment = successfulPaymentWithoutCurrency();

                mockJournalData(List.of(), List.of());

                JournalReconcliationResult result = reconcile(payment);

                assertFalse(result.valid());
                assertTrue(result.discrepancies().contains("MISSING_JOURNAL"));
        }

        // Duplicate journal entries
        @Test
        void shouldDetectDuplicateJournalEntries() {
                Payment payment = successfulPaymentWithoutCurrency();

                JournalEntries entry1 = duplicateJournalEntry();
                JournalEntries entry2 = duplicateJournalEntry();

                mockJournalData(List.of(entry1, entry2), List.of());

                JournalReconcliationResult result = reconcile(payment);

                assertFalse(result.valid());
                assertTrue(result.discrepancies().contains("DUPLICATE_JOURNAL"));
                assertEquals(2, result.journalEntryCount());
        }

        // Wrong debit and credit accounts
        @Test
        void shouldDetectWrongDebitAndCreditAccounts() {
                Payment payment = successfulPayment();
                JournalEntries entry = journalEntry(paymentId);

                List<JournalLines> lines = List.of(
                                journalLine(entry, merchantPayableId, JournalSides.DEBIT, paymentAmount, CURRENCY),
                                journalLine(entry, providerClearingId, JournalSides.CREDIT, paymentAmount, CURRENCY));

                mockJournalData(List.of(entry), lines);

                JournalReconcliationResult result = reconcile(payment);

                assertFalse(result.valid());
                assertTrue(result.discrepancies().contains("WRONG_DEBIT_ACCOUNT"));
                assertTrue(result.discrepancies().contains("WRONG_CREDIT_ACCOUNT"));
        }

        // Amount mismatch
        @Test
        void shouldDetectJournalAmountMismatch() {
                Payment payment = successfulPayment();
                JournalEntries entry = journalEntry(paymentId);

                List<JournalLines> lines = List.of(
                                journalLine(entry, providerClearingId, JournalSides.DEBIT, new BigDecimal("4000.00"),
                                                CURRENCY),
                                journalLine(entry, merchantPayableId, JournalSides.CREDIT, new BigDecimal("4000.00"),
                                                CURRENCY));

                mockJournalData(List.of(entry), lines);

                JournalReconcliationResult result = reconcile(payment);

                assertFalse(result.valid());
                assertTrue(result.discrepancies().contains("JOURNAL_AMOUNT_MISMATCH"));
        }

        // Currency mismatch
        @Test
        void shouldDetectCurrencyMismatch() {
                Payment payment = successfulPayment();
                JournalEntries entry = journalEntry(paymentId);

                List<JournalLines> lines = List.of(
                                journalLine(entry, providerClearingId, JournalSides.DEBIT, paymentAmount, "USD"),
                                journalLine(entry, merchantPayableId, JournalSides.CREDIT, paymentAmount, "USD"));

                mockJournalData(List.of(entry), lines);

                JournalReconcliationResult result = reconcile(payment);

                assertFalse(result.valid());
                assertTrue(result.discrepancies().contains("JOURNAL_CURRENCY_MISMATCH"));
        }

        // Unbalanced journal
        @Test
        void shouldDetectUnbalancedJournal() {
                Payment payment = successfulPayment();
                JournalEntries entry = journalEntry(paymentId);

                List<JournalLines> lines = List.of(
                                journalLine(entry, providerClearingId, JournalSides.DEBIT, new BigDecimal("5000.00"),
                                                CURRENCY),
                                journalLine(entry, merchantPayableId, JournalSides.CREDIT, new BigDecimal("4000.00"),
                                                CURRENCY));

                mockJournalData(List.of(entry), lines);

                JournalReconcliationResult result = reconcile(payment);

                assertFalse(result.valid());
                assertTrue(result.discrepancies().contains("UNBALANCED_JOURNAL"));
        }

        // Duplicate debit and credit lines
        @Test
        void shouldDetectDuplicateDebitAndCreditLines() {
                Payment payment = successfulPayment();
                JournalEntries entry = journalEntry(paymentId);

                BigDecimal halfAmount = new BigDecimal("2500.00");

                List<JournalLines> lines = List.of(
                                journalLine(entry, providerClearingId, JournalSides.DEBIT, halfAmount, CURRENCY),
                                journalLine(entry, providerClearingId, JournalSides.DEBIT, halfAmount, CURRENCY),
                                journalLine(entry, merchantPayableId, JournalSides.CREDIT, halfAmount, CURRENCY),
                                journalLine(entry, merchantPayableId, JournalSides.CREDIT, halfAmount, CURRENCY));

                mockJournalData(List.of(entry), lines);

                JournalReconcliationResult result = reconcile(payment);

                assertFalse(result.valid());
                assertTrue(result.discrepancies().contains("DUPLICATE_DEBIT_LINES"));
                assertTrue(result.discrepancies().contains("DUPLICATE_CREDIT_LINES"));
                assertTrue(result.discrepancies().contains("INVALID_JOURNAL_LINE_COUNT"));
        }

        // Invalid payment amount
        @Test
        void shouldRejectInvalidPaymentAmount() {
                Payment payment = mock(Payment.class);

                when(payment.getId()).thenReturn(paymentId);
                when(payment.getStatus()).thenReturn(PaymentStatus.SUCCEEDED);
                when(payment.getAmount()).thenReturn(BigDecimal.ZERO);

                JournalReconcliationResult result = reconcile(payment);

                assertFalse(result.valid());
                assertTrue(result.discrepancies().contains("INVALID_PAYMENT_AMOUNT"));

                verifyNoInteractions(journalEntryRepository, journalLineRepository);
        }

        // Pending and failed payments should be skipped
        @Test
        void shouldSkipPendingPayments() {
                Payment payment = mock(Payment.class);
                when(payment.getStatus()).thenReturn(PaymentStatus.PENDING);

                Map<UUID, JournalReconcliationResult> results = service.reconcileResults(List.of(payment));

                assertTrue(results.isEmpty());
                verifyNoInteractions(journalEntryRepository, journalLineRepository);
        }

        @Test
        void shouldSkipFailedPayments() {
                Payment payment = mock(Payment.class);
                when(payment.getStatus()).thenReturn(PaymentStatus.FAILED);

                Map<UUID, JournalReconcliationResult> results = service.reconcileResults(List.of(payment));

                assertTrue(results.isEmpty());
                verifyNoInteractions(journalEntryRepository, journalLineRepository);
        }

        // Source event ID mismatch
        @Test
        void shouldDetectSourceEventIdMismatch() {
                Payment payment = successfulPayment();
                JournalEntries entry = journalEntry(UUID.randomUUID());

                List<JournalLines> lines = List.of(
                                journalLine(entry, providerClearingId, JournalSides.DEBIT, paymentAmount, CURRENCY),
                                journalLine(entry, merchantPayableId, JournalSides.CREDIT, paymentAmount, CURRENCY));

                mockJournalData(List.of(entry), lines);

                JournalReconcliationResult result = reconcile(payment);

                assertFalse(result.valid());
                assertTrue(result.discrepancies().contains("SOURCE_EVENT_ID_MISMATCH"));
        }

        // ---------- Helper methods ----------

        private JournalReconcliationResult reconcile(Payment payment) {
                return service.reconcileResults(List.of(payment)).get(paymentId);
        }

        private Payment successfulPayment() {
                Payment payment = successfulPaymentWithoutCurrency();
                when(payment.getCurrency()).thenReturn(CURRENCY);
                return payment;
        }

        // For tests where the service returns before any line (and so currency) is
        // checked
        private Payment successfulPaymentWithoutCurrency() {
                Payment payment = mock(Payment.class);

                when(payment.getId()).thenReturn(paymentId);
                when(payment.getStatus()).thenReturn(PaymentStatus.SUCCEEDED);
                when(payment.getAmount()).thenReturn(paymentAmount);

                return payment;
        }

        private JournalEntries journalEntry(UUID sourceEventId) {
                JournalEntries entry = mock(JournalEntries.class);

                when(entry.getId()).thenReturn(UUID.randomUUID());
                when(entry.getSourceId()).thenReturn(paymentId);
                when(entry.getSourceEventId()).thenReturn(sourceEventId);

                return entry;
        }

        // Duplicate entries return early, so getSourceEventId() is never read: don't
        // stub it
        private JournalEntries duplicateJournalEntry() {
                JournalEntries entry = mock(JournalEntries.class);

                when(entry.getId()).thenReturn(UUID.randomUUID());
                when(entry.getSourceId()).thenReturn(paymentId);

                return entry;
        }

        private JournalLines journalLine(JournalEntries entry, UUID accountId, JournalSides side,
                        BigDecimal amount, String currency) {

                JournalLines line = mock(JournalLines.class);
                LedgerAccount account = mock(LedgerAccount.class);

                when(account.getId()).thenReturn(accountId);
                when(line.getJournalEntry()).thenReturn(entry);
                when(line.getLedgerAccount()).thenReturn(account);
                when(line.getSide()).thenReturn(side);
                when(line.getAmount()).thenReturn(amount);
                when(line.getCurrency()).thenReturn(currency);

                return line;
        }

        private void mockJournalData(List<JournalEntries> entries, List<JournalLines> lines) {
                when(journalEntryRepository
                                .findBySourceTypeAndSourceIdIn(eq("PAYMENT"), anyCollection()))
                                .thenReturn(entries);

                // The service only queries lines when at least one entry exists
                if (!entries.isEmpty()) {
                        when(journalLineRepository
                                        .findByJournalEntry_IdIn(anyCollection()))
                                        .thenReturn(lines);
                }
        }
}