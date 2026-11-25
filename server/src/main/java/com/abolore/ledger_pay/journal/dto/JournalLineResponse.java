package com.abolore.ledger_pay.journal.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.abolore.ledger_pay.journal.entity.JournalLines;
import com.abolore.ledger_pay.journal.entity.JournalSides;

public record JournalLineResponse(
        UUID id,
        UUID ledgerAccountId,
        String accountCode,
        JournalSides side,
        BigDecimal amount,
        String currency) {

    public static JournalLineResponse from(JournalLines line) {
        return new JournalLineResponse(
                line.getId(),
                line.getLedgerAccount().getId(),
                line.getLedgerAccount().getAccountCode(),
                line.getSide(),
                line.getAmount(),
                line.getCurrency());
    }
}
