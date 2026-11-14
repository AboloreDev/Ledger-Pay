package com.abolore.ledger_pay.reconciliation.entity;

import java.util.List;
import java.util.UUID;

public record JournalReconcliationResult(UUID paymentId, boolean valid, int journalEntryCount,
        List<String> discrepancies) {

}
