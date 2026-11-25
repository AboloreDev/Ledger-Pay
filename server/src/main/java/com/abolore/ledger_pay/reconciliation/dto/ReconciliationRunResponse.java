package com.abolore.ledger_pay.reconciliation.dto;

import java.time.Instant;
import java.util.UUID;

import com.abolore.ledger_pay.reconciliation.entity.ReconciliationRunStatus;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationTriggerType;

public record ReconciliationRunResponse(
        UUID id,
        String provider,
        Instant periodStart,
        Instant periodEnd,
        ReconciliationRunStatus status,
        ReconciliationTriggerType triggerType,
        String initiatedBy,
        int totalProviderTransactions,
        int totalInternalPayments,
        int matchedCount,
        int discrepancyCount,
        int journalsChecked,
        int validJournals,
        int invalidJournals,
        Instant startedAt,
        Instant completedAt,
        Instant createdAt,
        String failureReason) {
}