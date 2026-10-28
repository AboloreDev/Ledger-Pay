package com.abolore.ledger_pay.journal.entity;

import java.util.List;
import java.util.UUID;

public record JournalEntryRequest(
                String reference,
                String sourceType,
                UUID sourceId,
                UUID sourceEventId,
                List<JournalLineRequest> lines) {
}
