package com.abolore.ledger_pay.journal.entity;

import java.math.BigDecimal;
import java.util.UUID;

public record JournalLineRequest(
        UUID ledgerAccountId,
        String currency,
        JournalSides side,
        BigDecimal amount) {
}
