package com.abolore.ledger_pay.journal.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abolore.ledger_pay.common.exceptions.DomainException;
import com.abolore.ledger_pay.common.exceptions.ErrorCode;
import com.abolore.ledger_pay.journal.dto.JournalEntryResponse;
import com.abolore.ledger_pay.journal.dto.JournalLineResponse;
import com.abolore.ledger_pay.journal.entity.JournalEntries;
import com.abolore.ledger_pay.journal.entity.JournalLines;
import com.abolore.ledger_pay.journal.repository.JournalEntryRepository;
import com.abolore.ledger_pay.journal.repository.JournalLineRepository;

@Service
public class JournalService {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalLineRepository journalLineRepository;

    public JournalService(JournalEntryRepository journalEntryRepository, JournalLineRepository journalLineRepository) {
        this.journalEntryRepository = journalEntryRepository;
        this.journalLineRepository = journalLineRepository;
    }

    // Scoped to merchant
    @Transactional(readOnly = true)
    public Page<JournalEntryResponse> findAllJournalForMerchant(UUID merchantId, Pageable pageable) {

        Page<JournalEntries> journals = journalEntryRepository.findAllByMerchantId(merchantId, pageable);

        if (journals.isEmpty()) {
            return Page.empty(pageable);
        }

        List<UUID> journalIds = journals.getContent()
                .stream()
                .map(JournalEntries::getId)
                .toList();

        List<JournalLines> lines = journalLineRepository.findByJournalEntry_IdIn(journalIds);

        Map<UUID, List<JournalLineResponse>> linesByJournalId = lines.stream()
                .collect(Collectors.groupingBy(
                        line -> line.getJournalEntry().getId(),
                        Collectors.mapping(JournalLineResponse::from, Collectors.toList())));

        return journals.map(journal -> new JournalEntryResponse(
                journal.getId(),
                journal.getReference(),
                journal.getSourceType(),
                journal.getSourceId(),
                journal.getCreatedAt(),
                linesByJournalId.getOrDefault(journal.getId(), List.of())));
    }

    @Transactional(readOnly = true)
    public JournalEntryResponse getByReferenceForJournal(String reference, UUID merchantId) {

        JournalEntries journal = journalEntryRepository
                .findByReferenceAndMerchantId(reference, merchantId)
                .orElseThrow(() -> DomainException.of(ErrorCode.JOURNAL_ENTRY_NOT_FOUND, "Journal Entry not found"));

        List<JournalLines> lines = journalLineRepository.findByJournalEntry_IdIn(List.of(journal.getId()));

        return new JournalEntryResponse(
                journal.getId(),
                journal.getReference(),
                journal.getSourceType(),
                journal.getSourceId(),
                journal.getCreatedAt(),
                lines.stream().map(JournalLineResponse::from).toList());
    }

    @Transactional(readOnly = true)
    public JournalEntryResponse getByIdForMerchant(UUID journalId, UUID merchantId) {

        JournalEntries journal = journalEntryRepository
                .findByIdAndMerchantId(journalId, merchantId)
                .orElseThrow(() -> DomainException.of(ErrorCode.JOURNAL_ENTRY_NOT_FOUND, "Journal Entry not found"));

        List<JournalLines> lines = journalLineRepository.findByJournalEntry_IdIn(List.of(journal.getId()));

        return new JournalEntryResponse(
                journal.getId(),
                journal.getReference(),
                journal.getSourceType(),
                journal.getSourceId(),
                journal.getCreatedAt(),
                lines.stream().map(JournalLineResponse::from).toList());
    }

}
