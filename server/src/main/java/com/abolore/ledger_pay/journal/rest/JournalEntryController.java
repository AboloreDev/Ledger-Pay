package com.abolore.ledger_pay.journal.rest;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abolore.ledger_pay.journal.dto.JournalEntryResponse;
import com.abolore.ledger_pay.journal.entity.JournalEntries;
import com.abolore.ledger_pay.journal.entity.JournalEntryRequest;
import com.abolore.ledger_pay.journal.service.JournalPostingService;
import com.abolore.ledger_pay.journal.service.JournalService;
import com.abolore.ledger_pay.merchants.dto.MerchantPrincipal;

@RestController
@RequestMapping("/api/v1/journal-entries")
public class JournalEntryController {

    private final JournalPostingService journalPostingService;
    private final JournalService journalService;

    public JournalEntryController(JournalPostingService journalPostingService, JournalService journalService) {
        this.journalPostingService = journalPostingService;
        this.journalService = journalService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> postJournalEntry(@RequestBody JournalEntryRequest request) {

        JournalEntries savedEntry = journalPostingService.postJournalEntry(request);

        Map<String, Object> response = Map.of(
                "id", savedEntry.getId(),
                "reference", savedEntry.getReference(),
                "status", "POSTED");

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Merchant Scoped
    @GetMapping("/{id}/merchant")
    public ResponseEntity<JournalEntryResponse> getJournalById(@PathVariable UUID journalId,
            Authentication authentication) {

        MerchantPrincipal principal = (MerchantPrincipal) authentication.getPrincipal();

        return ResponseEntity.ok(journalService.getByIdForMerchant(journalId, principal.merchantId()));

    }

    @GetMapping("/{reference}/merchant")
    public ResponseEntity<JournalEntryResponse> getJournalByReference(@PathVariable String reference,
            Authentication authentication) {

        MerchantPrincipal principal = (MerchantPrincipal) authentication.getPrincipal();

        return ResponseEntity.ok(journalService.getByReferenceForJournal(reference, principal.merchantId()));

    }

    @GetMapping
    public ResponseEntity<Page<JournalEntryResponse>> getJournalHistory(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        MerchantPrincipal principal = (MerchantPrincipal) authentication.getPrincipal();

        return ResponseEntity.ok(journalService.findAllJournalForMerchant(principal.merchantId(), pageable));

    }
}
