package com.abolore.ledger_pay.reconciliation.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.abolore.ledger_pay.payment.entity.Payment;
import com.abolore.ledger_pay.reconciliation.entity.JournalReconcliationResult;
import com.abolore.ledger_pay.reconciliation.entity.PaymentMatchingResult;
import com.abolore.ledger_pay.reconciliation.entity.PaymentMatchingSummary;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationRecordType;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationRun;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationTriggerType;
import com.abolore.ledger_pay.reconciliation.entity.TransactionReconciliationResult;
import com.abolore.ledger_pay.reconciliation.repository.ReconciliationRunRepository;

@Service
public class TransactionReconciliationService {

        private static final String PROVIDER = "PAYSTACK";

        private final PaymentMatchingService paymentMatchingService;
        private final InternalPaymentFetchService internalPaymentFetchService;
        private final JournalReconciliationService journalReconciliationService;
        private final ReconciliationRunRepository runRepository;
        private final ReconciliationResultPersistenceService persistenceService;

        public TransactionReconciliationService(
                        PaymentMatchingService paymentMatchingService,
                        InternalPaymentFetchService internalPaymentFetchService,
                        JournalReconciliationService journalReconciliationService,
                        ReconciliationRunRepository runRepository,
                        ReconciliationResultPersistenceService persistenceService) {

                this.paymentMatchingService = paymentMatchingService;
                this.internalPaymentFetchService = internalPaymentFetchService;
                this.journalReconciliationService = journalReconciliationService;
                this.runRepository = runRepository;
                this.persistenceService = persistenceService;
        }

        public TransactionReconciliationResult reconcile(Instant periodStart, Instant periodEnd,
                        ReconciliationTriggerType reconciliationTriggerType, String initiatedBy) {

                // Create the run in the db
                ReconciliationRun run = ReconciliationRun.create(PROVIDER, periodStart, periodEnd,
                                reconciliationTriggerType, initiatedBy);

                runRepository.save(run);

                // Update the status to running
                run.start();
                runRepository.save(run);

                try {
                        // Match Paystack transactions against internal payments
                        PaymentMatchingSummary matchingSummary = paymentMatchingService.match(periodStart, periodEnd);

                        // Extract internal payment IDs from uniquely matched references
                        List<UUID> paymentIds = matchingSummary.results().stream()
                                        .filter(result -> result
                                                        .reconciliationRecordType() == ReconciliationRecordType.PAYSTACK_TRANSACTION)
                                        .map(PaymentMatchingResult::internalPaymentId)
                                        .filter(Objects::nonNull)
                                        .distinct()
                                        .toList();

                        // Retrieve the actual payment records
                        List<Payment> payments = internalPaymentFetchService.fetchPaymentsByIds(paymentIds);

                        // Reconcile their journal entries
                        Map<UUID, JournalReconcliationResult> journalMap = journalReconciliationService
                                        .reconcileResults(payments);

                        List<JournalReconcliationResult> journalResults = new ArrayList<>(journalMap.values());

                        // Calculate journal reconciliation totals
                        int checked = journalResults.size();

                        int valid = (int) journalResults.stream()
                                        .filter(JournalReconcliationResult::valid)
                                        .count();

                        int invalid = checked - valid;

                        TransactionReconciliationResult reconciliationResult = new TransactionReconciliationResult(
                                        matchingSummary, checked, valid, invalid, journalResults);

                        persistenceService.persistResultToDB(run, reconciliationResult);

                        // Return the combined result
                        return new TransactionReconciliationResult(
                                        matchingSummary,
                                        checked,
                                        valid,
                                        invalid,
                                        journalResults);
                } catch (Exception exception) {
                        run.fail(exception.getMessage());
                        runRepository.save(run);
                        throw exception;
                }

        }
}