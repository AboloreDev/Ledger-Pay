package com.abolore.ledger_pay.reconciliation.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.abolore.ledger_pay.reconciliation.entity.JournalReconcliationResult;
import com.abolore.ledger_pay.reconciliation.entity.PaymentMatchingResult;
import com.abolore.ledger_pay.reconciliation.entity.PaymentMatchingSummary;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationRecordType;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationResult;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationResultFactory;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationRun;
import com.abolore.ledger_pay.reconciliation.entity.TransactionReconciliationResult;
import com.abolore.ledger_pay.reconciliation.repository.ReconciliationResultRepository;
import com.abolore.ledger_pay.reconciliation.repository.ReconciliationRunRepository;

import jakarta.transaction.Transactional;

// Persist reconciliation in the db
@Service
public class ReconciliationResultPersistenceService {

    private final ReconciliationResultFactory reconciliationResultFactory;
    private final ReconciliationResultRepository reconciliationResultRepository;
    private final ReconciliationRunRepository reconciliationRunRepository;

    public ReconciliationResultPersistenceService(ReconciliationResultFactory reconciliationResultFactory,
            ReconciliationResultRepository reconciliationResultRepository,
            ReconciliationRunRepository reconciliationRunRepository) {
        this.reconciliationResultFactory = reconciliationResultFactory;
        this.reconciliationResultRepository = reconciliationResultRepository;
        this.reconciliationRunRepository = reconciliationRunRepository;
    }

    @Transactional
    public void persistResultToDB(ReconciliationRun reconciliationRun,
            TransactionReconciliationResult transactionReconciliationResult) {

        List<ReconciliationResult> reconciliationResults = new ArrayList<>();

        for (PaymentMatchingResult matchResult : transactionReconciliationResult.paymentMatchingSummary().results()) {
            ReconciliationResult result = reconciliationResultFactory
                    .createReconciliationResultThatConvertsJSOBDiscrepancyToString(
                            reconciliationRun,
                            matchResult.reconciliationRecordType(),
                            matchResult.reference(),
                            matchResult.internalPaymentId(),
                            matchResult.matched(),
                            matchResult.discrepancies());

            reconciliationResults.add(result);
        }

        for (JournalReconcliationResult journalResult : transactionReconciliationResult.journalResults()) {
            ReconciliationResult result = reconciliationResultFactory
                    .createReconciliationResultThatConvertsJSOBDiscrepancyToString(
                            reconciliationRun,
                            ReconciliationRecordType.JOURNAL,
                            null,
                            journalResult.paymentId(),
                            journalResult.valid(),
                            journalResult.discrepancies());

            reconciliationResults.add(result);
        }

        reconciliationResultRepository.saveAllAndFlush(reconciliationResults);

        // Calculate total discrepant records
        PaymentMatchingSummary matches = transactionReconciliationResult.paymentMatchingSummary();

        int discrepancies = matches.discrepantCount() + matches.unmatchedInternalCount()
                + matches.unmatchedPaystackCount() + transactionReconciliationResult.invalidJournalCount();

        reconciliationRun.complete(matches.totalPaystackTransactions(), matches.totalInternalPayments(),
                matches.matchedCount(), discrepancies, transactionReconciliationResult.journalCheckedCount(),
                transactionReconciliationResult.validJournalCount(),
                transactionReconciliationResult.invalidJournalCount(), false);

        reconciliationRunRepository.save(reconciliationRun);
    }
}
