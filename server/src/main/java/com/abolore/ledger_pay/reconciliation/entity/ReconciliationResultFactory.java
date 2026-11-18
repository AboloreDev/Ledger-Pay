package com.abolore.ledger_pay.reconciliation.entity;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class ReconciliationResultFactory {

    private final ObjectMapper objectMapper;

    public ReconciliationResultFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ReconciliationResult createReconciliationResultThatConvertsJSOBDiscrepancyToString(ReconciliationRun run,
            ReconciliationRecordType recordType, String reference, UUID paymentId, boolean matched,
            List<String> discrepancies) {

        try {
            String discrepancyJson = objectMapper.writeValueAsString(discrepancies);

            return ReconciliationResult.create(
                    run,
                    recordType,
                    reference,
                    paymentId,
                    matched,
                    discrepancyJson);

        } catch (JacksonException e) {
            throw new IllegalStateException(
                    "Failed to serialize reconciliation discrepancies", e);
        }
    }
}