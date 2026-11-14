package com.abolore.ledger_pay.reconciliation.entity;

import java.util.List;

public record PaymentMatchingSummary(int totalPaystackTransactions, int totalInternalPayments, int matchedCount,
                int discrepantCount, int unmatchedPaystackCount, int unmatchedInternalCount,
                List<PaymentMatchingResult> results) {
}