package com.abolore.ledger_pay.reconciliation.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abolore.ledger_pay.journal.entity.JournalEntries;
import com.abolore.ledger_pay.journal.entity.JournalLines;
import com.abolore.ledger_pay.journal.entity.JournalSides;
import com.abolore.ledger_pay.journal.repository.JournalEntryRepository;
import com.abolore.ledger_pay.journal.repository.JournalLineRepository;
import com.abolore.ledger_pay.payment.entity.Payment;
import com.abolore.ledger_pay.payment.entity.PaymentStatus;
import com.abolore.ledger_pay.reconciliation.entity.JournalReconcliationResult;

@Service
public class JournalReconciliationService {

    private static final String ACCOUNT_SOURCE_TYPE = "PAYMENT";

    private final JournalEntryRepository journalEntryRepository;
    private final JournalLineRepository journalLineRepository;

    private final UUID providerClearingAccountId;
    private final UUID merchantPayableAccountId;

    public JournalReconciliationService(
            JournalEntryRepository journalEntryRepository,
            JournalLineRepository journalLineRepository,
            @Value("${ledger.accounts.provider-clearing-id}") UUID providerClearingAccountId,
            @Value("${ledger.accounts.merchant-payable-id}") UUID merchantPayableAccountId) {
        this.journalEntryRepository = journalEntryRepository;
        this.journalLineRepository = journalLineRepository;
        this.providerClearingAccountId = providerClearingAccountId;
        this.merchantPayableAccountId = merchantPayableAccountId;
    }

    private JournalReconcliationResult invalidResult(UUID paymentId, String discrepancy) {
        return new JournalReconcliationResult(paymentId, false, 0, List.of(discrepancy));
    }

    @Transactional(readOnly = true)
    public Map<UUID, JournalReconcliationResult> reconcileResults(List<Payment> payments) {

        // Only successful payments are reconciled
        List<Payment> successfulPayments = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCEEDED)
                .toList();

        if (successfulPayments.isEmpty()) {
            return Map.of();
        }

        Map<UUID, JournalReconcliationResult> reconciliationResults = new HashMap<>();

        // Validate payment amount before querying journals
        List<Payment> validPayments = new ArrayList<>();

        for (Payment payment : successfulPayments) {
            if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                reconciliationResults.put(payment.getId(), invalidResult(payment.getId(), "INVALID_PAYMENT_AMOUNT"));
            } else {
                validPayments.add(payment);
            }
        }

        if (validPayments.isEmpty()) {
            return reconciliationResults;
        }

        Set<UUID> paymentIds = validPayments.stream()
                .map(Payment::getId)
                .collect(Collectors.toSet());

        // Fetch all journal entries for these payments
        List<JournalEntries> entries = journalEntryRepository
                .findBySourceTypeAndSourceIdIn(ACCOUNT_SOURCE_TYPE, paymentIds);

        Map<UUID, List<JournalEntries>> entriesByPaymentId = entries.stream()
                .collect(Collectors.groupingBy(JournalEntries::getSourceId));

        // Fetch all journal lines for those entries
        List<UUID> entryIds = entries.stream()
                .map(JournalEntries::getId)
                .toList();

        List<JournalLines> lines = entryIds.isEmpty()
                ? List.of()
                : journalLineRepository.findByJournalEntry_IdIn(entryIds);

        Map<UUID, List<JournalLines>> linesByEntryId = lines.stream()
                .collect(Collectors.groupingBy(line -> line.getJournalEntry().getId()));

        // Validate each payment against its entries and lines
        for (Payment payment : validPayments) {
            List<JournalEntries> paymentEntries = entriesByPaymentId.getOrDefault(payment.getId(), List.of());

            reconciliationResults.put(payment.getId(), validatePayment(payment, paymentEntries, linesByEntryId));
        }

        return reconciliationResults;
    }

    // Payment validation helper
    private JournalReconcliationResult validatePayment(Payment payment, List<JournalEntries> entries,
            Map<UUID, List<JournalLines>> linesByEntryId) {

        List<String> discrepancies = new ArrayList<>();

        if (entries.isEmpty()) {
            discrepancies.add("MISSING_JOURNAL");
            return new JournalReconcliationResult(payment.getId(), false, 0, discrepancies);
        }

        if (entries.size() > 1) {
            discrepancies.add("DUPLICATE_JOURNAL");
            return new JournalReconcliationResult(payment.getId(), false, entries.size(), discrepancies);
        }

        JournalEntries entry = entries.get(0);

        if (!payment.getId().equals(entry.getSourceEventId())) {
            discrepancies.add("SOURCE_EVENT_ID_MISMATCH");
        }

        List<JournalLines> lines = linesByEntryId.getOrDefault(entry.getId(), List.of());

        if (lines.size() != 2) {
            discrepancies.add("INVALID_JOURNAL_LINE_COUNT");
        }

        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        int debitCount = 0;
        int creditCount = 0;

        int providerClearingDebitCount = 0;
        int merchantPayableCreditCount = 0;

        for (JournalLines line : lines) {

            if (line.getAmount() == null || line.getAmount().signum() <= 0) {
                discrepancies.add("INVALID_JOURNAL_AMOUNT");
                continue;
            }

            if (line.getAmount().compareTo(payment.getAmount()) != 0) {
                discrepancies.add("JOURNAL_AMOUNT_MISMATCH");
            }

            if (!normalizeCurrency(payment.getCurrency())
                    .equals(normalizeCurrency(line.getCurrency()))) {
                discrepancies.add("JOURNAL_CURRENCY_MISMATCH");
            }

            UUID accountId = line.getLedgerAccount().getId();

            if (line.getSide() == JournalSides.DEBIT) {

                debitCount++;
                totalDebits = totalDebits.add(line.getAmount());

                if (providerClearingAccountId.equals(accountId)) {
                    providerClearingDebitCount++;
                } else {
                    discrepancies.add("WRONG_DEBIT_ACCOUNT");
                }

            } else if (line.getSide() == JournalSides.CREDIT) {

                creditCount++;
                totalCredits = totalCredits.add(line.getAmount());

                if (merchantPayableAccountId.equals(accountId)) {
                    merchantPayableCreditCount++;
                } else {
                    discrepancies.add("WRONG_CREDIT_ACCOUNT");
                }

            } else {
                discrepancies.add("INVALID_JOURNAL_SIDE");
            }
        }

        if (debitCount > 1) {
            discrepancies.add("DUPLICATE_DEBIT_LINES");
        }

        if (creditCount > 1) {
            discrepancies.add("DUPLICATE_CREDIT_LINES");
        }

        if (debitCount == 0) {
            discrepancies.add("MISSING_DEBIT_LINE");
        }

        if (creditCount == 0) {
            discrepancies.add("MISSING_CREDIT_LINE");
        }

        if (providerClearingDebitCount == 0) {
            discrepancies.add("MISSING_PROVIDER_CLEARING_DEBIT");
        }

        if (providerClearingDebitCount > 1) {
            discrepancies.add("DUPLICATE_PROVIDER_CLEARING_DEBIT");
        }

        if (merchantPayableCreditCount == 0) {
            discrepancies.add("MISSING_MERCHANT_PAYABLE_CREDIT");
        }

        if (merchantPayableCreditCount > 1) {
            discrepancies.add("DUPLICATE_MERCHANT_PAYABLE_CREDIT");
        }

        if (totalDebits.compareTo(totalCredits) != 0) {
            discrepancies.add("UNBALANCED_JOURNAL");
        }

        return new JournalReconcliationResult(payment.getId(), discrepancies.isEmpty(), entries.size(),
                discrepancies.stream().distinct().toList());
    }

    private String normalizeCurrency(String currency) {
        return currency == null ? "" : currency.trim().toUpperCase(Locale.ROOT);
    }
}