package com.abolore.ledger_pay.reconciliation.mapper;

import org.springframework.stereotype.Component;

import com.abolore.ledger_pay.reconciliation.dto.ReconciliationRunResponse;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationRun;

@Component
public class ReconciliationRunMapper {

    public ReconciliationRunResponse toResponse(ReconciliationRun run) {
        return new ReconciliationRunResponse(
                run.getId(),
                run.getProvider(),
                run.getPeriodStart(),
                run.getPeriodEnd(),
                run.getStatus(),
                run.getTriggerType(),
                run.getInitiatedBy(),
                run.getTotalProviderTransactions(),
                run.getTotalInternalPayments(),
                run.getMatchedCount(),
                run.getDiscrepancyCount(),
                run.getJournalsChecked(),
                run.getValidJournals(),
                run.getInvalidJournals(),
                run.getStartedAt(),
                run.getCompletedAt(),
                run.getCreatedAt(),
                run.getFailureReason());
    }
}