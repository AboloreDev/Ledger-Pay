package com.abolore.ledger_pay.journal.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.abolore.ledger_pay.common.exceptions.DomainException;
import com.abolore.ledger_pay.common.exceptions.ErrorCode;
import com.abolore.ledger_pay.journal.entity.JournalEntries;
import com.abolore.ledger_pay.journal.entity.JournalEntryRequest;
import com.abolore.ledger_pay.journal.entity.JournalLineRequest;
import com.abolore.ledger_pay.journal.entity.JournalLines;
import com.abolore.ledger_pay.journal.entity.JournalSides;
import com.abolore.ledger_pay.journal.repository.JournalEntryRepository;
import com.abolore.ledger_pay.journal.repository.JournalLineRepository;
import com.abolore.ledger_pay.ledger.entity.LedgerAccount;
import com.abolore.ledger_pay.ledger.entity.LedgerAccountStatus;
import com.abolore.ledger_pay.ledger.repository.LedgerAccountRepository;

import jakarta.transaction.Transactional;

@Service
public class JournalPostingService {

    private final JournalEntryRepository journalEntryRepository;
    private final LedgerAccountRepository ledgerAccountRepository;
    private final JournalLineRepository journalLineRepository;

    public JournalPostingService(
            JournalEntryRepository journalEntryRepository,
            LedgerAccountRepository ledgerAccountRepository,
            JournalLineRepository journalLineRepository) {
        this.journalEntryRepository = journalEntryRepository;
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.journalLineRepository = journalLineRepository;
    }

    @Transactional
    public JournalEntries postJournalEntry(JournalEntryRequest entryRequest) {

        if (entryRequest == null
                || entryRequest.reference() == null
                || entryRequest.reference().isBlank()
                || entryRequest.sourceType() == null
                || entryRequest.sourceType().isBlank()
                || entryRequest.sourceId() == null
                || entryRequest.sourceEventId() == null) {
            throw DomainException.of(ErrorCode.JOURNAL_BAD_REQUEST,
                    "Journal entry Request fields must be provided.");
        }

        boolean alreadyPosted = journalEntryRepository.existsBySourceTypeAndSourceEventId(
                entryRequest.sourceType(), entryRequest.sourceEventId());

        if (alreadyPosted) {
            throw DomainException.of(ErrorCode.DUPLICATE_SOURCE_POSTING,
                    "This source event has already been posted.");
        }

        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        List<JournalLineRequest> validatedLines = new ArrayList<>();
        List<LedgerAccount> validatedAccounts = new ArrayList<>();

        List<JournalLineRequest> requestLines = entryRequest.lines();

        if (requestLines == null || requestLines.size() < 2) {
            throw DomainException.of(ErrorCode.UNBALANCED_JOURNAL_ENTRY,
                    "A journal entry must contain at least two lines.");
        }

        for (JournalLineRequest line : requestLines) {
            if (line == null
                    || line.ledgerAccountId() == null
                    || line.side() == null
                    || line.amount() == null
                    || line.currency() == null
                    || !line.currency().matches("[A-Z]{3}")) {
                throw DomainException.of(ErrorCode.JOURNAL_BAD_REQUEST,
                        "Each line must have a valid account, side, amount and currency.");
            }

            if (line.amount().signum() <= 0
                    || line.amount().stripTrailingZeros().scale() > 2) {
                throw DomainException.of(ErrorCode.JOURNAL_BAD_REQUEST,
                        "Amount must be positive and have at most two decimal places.");
            }

            LedgerAccount account = ledgerAccountRepository
                    .findById(line.ledgerAccountId())
                    .orElseThrow(() -> DomainException.of(ErrorCode.LEDGER_NOT_FOUND,
                            "Ledger account not found: " + line.ledgerAccountId()));

            if (account.getStatus() != LedgerAccountStatus.ACTIVE) {
                throw DomainException.of(ErrorCode.JOURNAL_BAD_REQUEST,
                        "Cannot post to an inactive ledger account.");
            }

            if (!account.getCurrency().equals(line.currency())) {
                throw DomainException.of(ErrorCode.JOURNAL_BAD_REQUEST,
                        "Journal line currency does not match the account currency.");
            }

            if (line.side() == JournalSides.DEBIT) {
                totalDebits = totalDebits.add(line.amount());
            } else {
                totalCredits = totalCredits.add(line.amount());
            }

            validatedLines.add(line);
            validatedAccounts.add(account);
        }

        if (totalDebits.compareTo(totalCredits) != 0) {
            throw DomainException.of(ErrorCode.UNBALANCED_JOURNAL_ENTRY,
                    "Journal entry is unbalanced. Debits must equal credits.");
        }

        JournalEntries entry = new JournalEntries.Builder()
                .setReference(entryRequest.reference())
                .setSourceId(entryRequest.sourceId())
                .setSourceType(entryRequest.sourceType())
                .setSourceEventId(entryRequest.sourceEventId())
                .build();

        JournalEntries savedEntry = journalEntryRepository.save(entry);

        List<JournalLines> journalLines = new ArrayList<>();

        for (int i = 0; i < validatedLines.size(); i++) {
            JournalLineRequest line = validatedLines.get(i);
            LedgerAccount account = validatedAccounts.get(i);

            JournalLines newJournalLines = new JournalLines.Builder()
                    .setJournalEntry(savedEntry)
                    .setLedgerAccount(account)
                    .setCurrency(line.currency())
                    .setSide(line.side())
                    .setAmount(line.amount())
                    .build();

            journalLines.add(newJournalLines);
        }

        journalLineRepository.saveAll(journalLines);

        return savedEntry;
    }
}
