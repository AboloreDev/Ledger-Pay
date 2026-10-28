package com.abolore.ledger_pay.journal.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.abolore.ledger_pay.journal.entity.JournalEntries;
import com.abolore.ledger_pay.journal.entity.JournalEntryRequest;
import com.abolore.ledger_pay.journal.entity.JournalLineRequest;
import com.abolore.ledger_pay.journal.entity.JournalLines;
import com.abolore.ledger_pay.journal.entity.JournalSides;
import com.abolore.ledger_pay.journal.exceptions.DuplicateSourceEventException;
import com.abolore.ledger_pay.journal.repository.JournalEntryRepository;
import com.abolore.ledger_pay.journal.repository.JournalLineRepository;
import com.abolore.ledger_pay.ledger.entity.LedgerAccount;
import com.abolore.ledger_pay.ledger.entity.LedgerAccountStatus;
import com.abolore.ledger_pay.ledger.entity.LedgerAccountType;
import com.abolore.ledger_pay.ledger.repository.LedgerAccountRepository;

@ExtendWith(MockitoExtension.class)
public class JournalPostingServiceTest {

        @Mock
        private JournalEntryRepository journalEntryRepository;

        @Mock
        private JournalLineRepository journalLineRepository;

        @Mock
        private LedgerAccountRepository ledgerAccountRepository;

        @InjectMocks
        private JournalPostingService journalPostingService;

        @Test
        void shouldRejectUnbalancedJournalEntry() {

                UUID debitAccountId = generateRandomUUID();
                UUID creditAccountId = generateRandomUUID();

                when(journalEntryRepository
                                .existsBySourceTypeAndSourceEventId(
                                                anyString(), any(UUID.class)))
                                .thenReturn(false);

                when(ledgerAccountRepository.findById(debitAccountId))
                                .thenReturn(Optional.of(activeNgnAccount(debitAccountId)));

                when(ledgerAccountRepository.findById(creditAccountId))
                                .thenReturn(Optional.of(activeNgnAccount(creditAccountId)));

                JournalEntryRequest request = requestWithLines(unBalancedEntryLines(debitAccountId, creditAccountId));

                assertThrows(
                                IllegalArgumentException.class,
                                () -> journalPostingService.postJournalEntry(request));

                verify(journalEntryRepository, never()).save(any());
                verify(journalLineRepository, never()).saveAll(anyList());
        }

        @Test
        void shouldAcceptBalancedJournalPosting() {

                UUID debitAccountId = generateRandomUUID();
                UUID creditAccountId = generateRandomUUID();

                when(journalEntryRepository
                                .existsBySourceTypeAndSourceEventId(anyString(), any(UUID.class)))
                                .thenReturn(false);

                when(ledgerAccountRepository.findById(debitAccountId))
                                .thenReturn(Optional.of(activeNgnAccount(debitAccountId)));

                when(ledgerAccountRepository.findById(creditAccountId))
                                .thenReturn(Optional.of(activeNgnAccount(creditAccountId)));

                when(journalEntryRepository.save(any(JournalEntries.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                JournalEntryRequest request = requestWithLines(balancedEntryLines(debitAccountId, creditAccountId));

                JournalEntries result = journalPostingService.postJournalEntry(request);

                assertNotNull(result);

                verify(journalEntryRepository, times(1))
                                .save(any(JournalEntries.class));

                verify(journalLineRepository, times(1))
                                .saveAll(argThat(lines -> {
                                        int count = 0;
                                        for (JournalLines line : lines) {
                                                count++;
                                        }
                                        return count == 2;
                                }));
        }

        @Test
        void shouldRejectDuplicateSourceEvent() {
                JournalEntryRequest request = requestWithLines(
                                balancedEntryLines(generateRandomUUID(), generateRandomUUID()));

                when(journalEntryRepository.existsBySourceTypeAndSourceEventId(
                                request.sourceType(), request.sourceEventId()))
                                .thenReturn(true);

                assertThrows(
                                DuplicateSourceEventException.class,
                                () -> journalPostingService.postJournalEntry(request));

                verifyNoJournalWrites();
        }

        @Test
        void shouldRejectMissingLedgerAccount() {
                UUID missingAccountId = UUID.randomUUID();

                when(journalEntryRepository.existsBySourceTypeAndSourceEventId(
                                anyString(), any(UUID.class)))
                                .thenReturn(false);

                when(ledgerAccountRepository.findById(missingAccountId))
                                .thenReturn(Optional.empty());

                JournalEntryRequest request = requestWithLines(
                                balancedEntryLines(missingAccountId, generateRandomUUID()));

                assertThrows(
                                IllegalArgumentException.class,
                                () -> journalPostingService.postJournalEntry(request));

                verifyNoJournalWrites();
        }

        @Test
        void shouldRejectInactiveLedgerAccount() {
                UUID inactiveAccountId = UUID.randomUUID();

                when(journalEntryRepository.existsBySourceTypeAndSourceEventId(
                                anyString(), any(UUID.class)))
                                .thenReturn(false);

                when(ledgerAccountRepository.findById(inactiveAccountId))
                                .thenReturn(Optional.of(
                                                testAccount(inactiveAccountId, "NGN", LedgerAccountStatus.INACTIVE)));

                JournalEntryRequest request = requestWithLines(
                                balancedEntryLines(inactiveAccountId, generateRandomUUID()));

                assertThrows(
                                IllegalArgumentException.class,
                                () -> journalPostingService.postJournalEntry(request));

                verifyNoJournalWrites();
        }

        @Test
        void shouldRejectAccountCurrencyMismatch() {
                UUID accountId = UUID.randomUUID();

                when(journalEntryRepository.existsBySourceTypeAndSourceEventId(
                                anyString(), any(UUID.class)))
                                .thenReturn(false);

                // The account uses NGN, but the journal line requests USD.
                when(ledgerAccountRepository.findById(accountId))
                                .thenReturn(Optional.of(
                                                testAccount(accountId, "NGN", LedgerAccountStatus.ACTIVE)));

                JournalEntryRequest request = requestWithLines(
                                List.of(
                                                new JournalLineRequest(
                                                                accountId, "USD", JournalSides.DEBIT,
                                                                new BigDecimal("100.00")),
                                                new JournalLineRequest(
                                                                UUID.randomUUID(), "NGN", JournalSides.CREDIT,
                                                                new BigDecimal("100.00"))));

                assertThrows(
                                IllegalArgumentException.class,
                                () -> journalPostingService.postJournalEntry(request));

                verifyNoJournalWrites();
        }

        @Test
        void shouldRejectZeroAmount() {
                UUID debitAccountId = UUID.randomUUID();
                UUID creditAccountId = UUID.randomUUID();

                when(journalEntryRepository.existsBySourceTypeAndSourceEventId(
                                anyString(), any(UUID.class)))
                                .thenReturn(false);

                JournalEntryRequest request = requestWithLines(List.of(
                                new JournalLineRequest(
                                                debitAccountId, "NGN", JournalSides.DEBIT,
                                                BigDecimal.ZERO),
                                new JournalLineRequest(
                                                creditAccountId, "NGN", JournalSides.CREDIT,
                                                new BigDecimal("100.00"))));

                assertThrows(
                                IllegalArgumentException.class,
                                () -> journalPostingService.postJournalEntry(request));

                verifyNoJournalWrites();
        }

        @Test
        void shouldRejectNegativeAmount() {
                UUID debitAccountId = UUID.randomUUID();
                UUID creditAccountId = UUID.randomUUID();

                when(journalEntryRepository.existsBySourceTypeAndSourceEventId(
                                anyString(), any(UUID.class)))
                                .thenReturn(false);

                JournalEntryRequest request = requestWithLines(
                                List.of(
                                                new JournalLineRequest(
                                                                debitAccountId, "NGN", JournalSides.DEBIT,
                                                                new BigDecimal("-100.00")),
                                                new JournalLineRequest(
                                                                creditAccountId, "NGN", JournalSides.CREDIT,
                                                                new BigDecimal("100.00"))));

                assertThrows(
                                IllegalArgumentException.class,
                                () -> journalPostingService.postJournalEntry(request));

                verifyNoJournalWrites();
        }

        // TTESTING HELPER METHODS
        private LedgerAccount activeNgnAccount(UUID id) {

                // helper to your actual LedgerAccount builder instead of rewriting.
                LedgerAccount ledger = new LedgerAccount.Builder()
                                .setAccountCode("TEST-" + id)
                                .setName("TEST ACCOUNT")
                                .setCurrency("NGN")
                                .setStatus(LedgerAccountStatus.ACTIVE)
                                .setAccountType(LedgerAccountType.LIABILITY)
                                .build();

                ReflectionTestUtils.setField(ledger, "id", id);

                return ledger;

        }

        private UUID generateRandomUUID() {
                return UUID.randomUUID();
        }

        private JournalEntryRequest requestWithLines(
                        List<JournalLineRequest> lines) {

                return new JournalEntryRequest(
                                "TEST-" + UUID.randomUUID(),
                                "PAYMENT",
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                lines);
        }

        private LedgerAccount testAccount(
                        UUID id,
                        String currency,
                        LedgerAccountStatus status) {

                LedgerAccount ledger = new LedgerAccount.Builder()
                                .setAccountCode("TEST-" + id)
                                .setName("Test Account")
                                .setCurrency(currency)
                                .setAccountType(LedgerAccountType.ASSET)
                                .setStatus(status)
                                .build();

                return ledger;
        }

        private void verifyNoJournalWrites() {
                verify(journalEntryRepository, never())
                                .save(any(JournalEntries.class));

                verify(journalLineRepository, never())
                                .saveAll(any());
        }

        private List<JournalLineRequest> balancedEntryLines(UUID debitAccountId, UUID creditAccountId) {
                return new ArrayList<>(List.of(
                                new JournalLineRequest(
                                                debitAccountId, "NGN",
                                                JournalSides.DEBIT,
                                                new BigDecimal("10000.00")),
                                new JournalLineRequest(
                                                creditAccountId, "NGN",
                                                JournalSides.CREDIT,
                                                new BigDecimal("10000.00"))));
        }

        private List<JournalLineRequest> unBalancedEntryLines(UUID debitAccountId, UUID creditAccountId) {
                return new ArrayList<>(List.of(
                                new JournalLineRequest(
                                                debitAccountId, "NGN",
                                                JournalSides.DEBIT,
                                                new BigDecimal("10000.00")),
                                new JournalLineRequest(
                                                creditAccountId, "NGN",
                                                JournalSides.CREDIT,
                                                new BigDecimal("9000.00"))));
        }

}
