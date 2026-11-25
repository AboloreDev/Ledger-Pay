
package com.abolore.ledger_pay.journal.service;

import com.abolore.ledger_pay.journal.entity.JournalEntries;
import com.abolore.ledger_pay.journal.entity.JournalLines;
import com.abolore.ledger_pay.journal.entity.JournalSides;
import com.abolore.ledger_pay.journal.repository.JournalEntryRepository;
import com.abolore.ledger_pay.journal.repository.JournalLineRepository;
import com.abolore.ledger_pay.common.exceptions.DomainException;
import com.abolore.ledger_pay.journal.dto.JournalEntryResponse;
import com.abolore.ledger_pay.ledger.entity.LedgerAccount;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JournalServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private JournalLineRepository journalLineRepository;

    @InjectMocks
    private JournalService journalService;

    private UUID merchantId;
    private UUID journalId;

    @BeforeEach
    void setUp() {
        merchantId = UUID.randomUUID();
        journalId = UUID.randomUUID();
    }

    private JournalEntries createJournal() {
        JournalEntries journal = mock(JournalEntries.class);

        when(journal.getId()).thenReturn(journalId);
        when(journal.getReference()).thenReturn("PAYMENT-001");
        when(journal.getSourceType()).thenReturn("PAYMENT");
        when(journal.getSourceId()).thenReturn(UUID.randomUUID());
        when(journal.getCreatedAt()).thenReturn(Instant.now());

        return journal;
    }

    private JournalLines createJournalLine(UUID entryId) {
        JournalLines line = mock(JournalLines.class);

        LedgerAccount account = mock(LedgerAccount.class);
        when(account.getId()).thenReturn(UUID.randomUUID());
        when(account.getAccountCode()).thenReturn("PROVIDER_CLEARING");

        when(line.getLedgerAccount()).thenReturn(account);
        when(line.getSide()).thenReturn(JournalSides.DEBIT);
        when(line.getAmount()).thenReturn(new BigDecimal("5000.00"));
        when(line.getCurrency()).thenReturn("NGN");

        return line;
    }

    @Test
    void shouldReturnMerchantJournalEntriesWithLines() {
        Pageable pageable = PageRequest.of(0, 10);

        JournalEntries journal = createJournal();

        JournalLines line = createJournalLine(journalId);

        JournalEntries entry = mock(JournalEntries.class);
        when(entry.getId()).thenReturn(journalId);
        when(line.getJournalEntry()).thenReturn(entry);

        when(journalEntryRepository.findAllByMerchantId(merchantId, pageable))
                .thenReturn(new PageImpl<>(List.of(journal), pageable, 1));

        when(journalLineRepository.findByJournalEntry_IdIn(List.of(journalId)))
                .thenReturn(List.of(line));

        Page<JournalEntryResponse> result = journalService.findAllJournalForMerchant(merchantId, pageable);

        assertEquals(1, result.getTotalElements());

        JournalEntryResponse response = result.getContent().get(0);

        assertEquals(journalId, response.id());
        assertEquals("PAYMENT-001", response.reference());
        assertEquals(1, response.lines().size());
        assertEquals("PROVIDER_CLEARING", response.lines().get(0).accountCode());

        verify(journalEntryRepository).findAllByMerchantId(merchantId, pageable);
        verify(journalLineRepository).findByJournalEntry_IdIn(List.of(journalId));
    }

    @Test
    void shouldReturnEmptyPageWhenMerchantHasNoJournals() {
        Pageable pageable = PageRequest.of(0, 10);

        when(journalEntryRepository.findAllByMerchantId(merchantId, pageable))
                .thenReturn(Page.empty(pageable));

        Page<JournalEntryResponse> result = journalService.findAllJournalForMerchant(merchantId, pageable);

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());

        verify(journalEntryRepository).findAllByMerchantId(merchantId, pageable);
        verify(journalLineRepository, never()).findByJournalEntry_IdIn(any());
    }

    @Test
    void shouldFetchLinesForMultipleJournalsInOneQuery() {
        Pageable pageable = PageRequest.of(0, 10);

        UUID secondJournalId = UUID.randomUUID();

        JournalEntries firstJournal = createJournal();

        JournalEntries secondJournal = mock(JournalEntries.class);
        when(secondJournal.getId()).thenReturn(secondJournalId);
        when(secondJournal.getReference()).thenReturn("PAYMENT-002");
        when(secondJournal.getSourceType()).thenReturn("PAYMENT");
        when(secondJournal.getSourceId()).thenReturn(UUID.randomUUID());
        when(secondJournal.getCreatedAt()).thenReturn(Instant.now());

        JournalLines firstLine = createJournalLine(journalId);
        JournalLines secondLine = createJournalLine(secondJournalId);

        JournalEntries firstEntry = mock(JournalEntries.class);
        when(firstEntry.getId()).thenReturn(journalId);
        when(firstLine.getJournalEntry()).thenReturn(firstEntry);

        JournalEntries secondEntry = mock(JournalEntries.class);
        when(secondEntry.getId()).thenReturn(secondJournalId);
        when(secondLine.getJournalEntry()).thenReturn(secondEntry);

        when(journalEntryRepository.findAllByMerchantId(merchantId, pageable))
                .thenReturn(new PageImpl<>(
                        List.of(firstJournal, secondJournal), pageable, 2));

        when(journalLineRepository.findByJournalEntry_IdIn(
                List.of(journalId, secondJournalId)))
                .thenReturn(List.of(firstLine, secondLine));

        Page<JournalEntryResponse> result = journalService.findAllJournalForMerchant(merchantId, pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getContent().get(0).lines().size());
        assertEquals(1, result.getContent().get(1).lines().size());

        verify(journalLineRepository, times(1))
                .findByJournalEntry_IdIn(List.of(journalId, secondJournalId));
    }

    @Test
    void shouldReturnJournalByIdForMerchantWithLines() {
        JournalEntries journal = createJournal();
        JournalLines line = createJournalLine(journalId);

        when(journalEntryRepository.findByIdAndMerchantId(journalId, merchantId))
                .thenReturn(Optional.of(journal));

        when(journalLineRepository.findByJournalEntry_IdIn(List.of(journalId)))
                .thenReturn(List.of(line));

        JournalEntryResponse result = journalService.getByIdForMerchant(journalId, merchantId);

        assertEquals(journalId, result.id());
        assertEquals("PAYMENT-001", result.reference());
        assertEquals(1, result.lines().size());

        verify(journalEntryRepository).findByIdAndMerchantId(journalId, merchantId);
        verify(journalLineRepository).findByJournalEntry_IdIn(List.of(journalId));
    }

    @Test
    void shouldRejectJournalIdNotOwnedByMerchant() {
        when(journalEntryRepository.findByIdAndMerchantId(journalId, merchantId))
                .thenReturn(Optional.empty());

        assertThrows(
                DomainException.class,
                () -> journalService.getByIdForMerchant(journalId, merchantId));

        verify(journalEntryRepository).findByIdAndMerchantId(journalId, merchantId);
        verifyNoInteractions(journalLineRepository);
    }

    @Test
    void shouldReturnJournalByReferenceForMerchantWithLines() {
        String reference = "PAYMENT-001";

        JournalEntries journal = createJournal();
        JournalLines line = createJournalLine(journalId);

        when(journalEntryRepository.findByReferenceAndMerchantId(reference, merchantId))
                .thenReturn(Optional.of(journal));

        when(journalLineRepository.findByJournalEntry_IdIn(List.of(journalId)))
                .thenReturn(List.of(line));

        JournalEntryResponse result = journalService.getByReferenceForJournal(reference, merchantId);

        assertEquals(journalId, result.id());
        assertEquals(reference, result.reference());
        assertEquals(1, result.lines().size());

        verify(journalEntryRepository)
                .findByReferenceAndMerchantId(reference, merchantId);
    }

    @Test
    void shouldRejectJournalReferenceNotOwnedByMerchant() {
        String reference = "PAYMENT-001";

        when(journalEntryRepository.findByReferenceAndMerchantId(reference, merchantId))
                .thenReturn(Optional.empty());

        assertThrows(
                DomainException.class,
                () -> journalService.getByReferenceForJournal(reference, merchantId));

        verify(journalEntryRepository)
                .findByReferenceAndMerchantId(reference, merchantId);

        verifyNoInteractions(journalLineRepository);
    }
}
