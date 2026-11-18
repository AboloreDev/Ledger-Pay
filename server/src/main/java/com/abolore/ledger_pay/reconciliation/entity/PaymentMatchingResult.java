package com.abolore.ledger_pay.reconciliation.entity;

import java.util.List;
import java.util.UUID;

public record PaymentMatchingResult(ReconciliationRecordType reconciliationRecordType, String reference,
        UUID internalPaymentId, boolean matched, List<String> discrepancies) {
}
