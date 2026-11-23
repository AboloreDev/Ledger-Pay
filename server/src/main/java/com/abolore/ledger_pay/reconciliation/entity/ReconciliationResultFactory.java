package com.abolore.ledger_pay.reconciliation.entity;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;

@Component
public class ReconciliationResultFactory {

    public ReconciliationResult createReconciliationResultThatConvertsJSOBDiscrepancyToString(ReconciliationRun run,
            ReconciliationRecordType recordType, String reference, UUID paymentId, boolean matched,
            List<String> discrepancies) {

        try {

            return ReconciliationResult.create(
                    run,
                    recordType,
                    reference,
                    paymentId,
                    matched,
                    discrepancies);

        } catch (JacksonException e) {
            throw new IllegalStateException(
                    "Failed to serialize reconciliation discrepancies", e);
        }
    }
}