package com.abolore.ledger_pay.journal.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record JournalEntryResponse(
        UUID id,
        String reference,
        String sourceType,
        UUID sourceId,
        Instant createdAt,
        List<JournalLineResponse> lines) {

}
