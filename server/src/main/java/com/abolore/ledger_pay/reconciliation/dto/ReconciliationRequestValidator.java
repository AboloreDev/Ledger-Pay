package com.abolore.ledger_pay.reconciliation.dto;

import java.time.Instant;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record ReconciliationRequestValidator(

        @NotNull(message = "Period start is required") Instant periodStart,

        @NotNull(message = "Period end is required") Instant periodEnd

) {
    @AssertTrue(message = "Period start must be before period end")
    public boolean isValidPeriod() {
        return periodStart != null
                && periodEnd != null
                && periodStart.isBefore(periodEnd);
    }
}
