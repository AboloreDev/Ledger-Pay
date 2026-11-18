package com.abolore.ledger_pay.reconciliation.entity;

import java.util.List;

public record TransactionReconciliationResult(
        PaymentMatchingSummary paymentMatchingSummary,
        int journalCheckedCount,
        int validJournalCount,
        int invalidJournalCount,
        List<JournalReconcliationResult> journalResults) {
}