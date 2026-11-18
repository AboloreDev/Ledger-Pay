package com.abolore.ledger_pay.reconciliation.dto;

import java.time.Instant;

public record ReconciliationRequest(Instant periodStart, Instant periodEnd) {
}