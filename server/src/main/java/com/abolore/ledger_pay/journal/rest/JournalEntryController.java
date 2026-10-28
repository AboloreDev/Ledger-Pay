package com.abolore.ledger_pay.journal.rest;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abolore.ledger_pay.journal.entity.JournalEntries;
import com.abolore.ledger_pay.journal.entity.JournalEntryRequest;
import com.abolore.ledger_pay.journal.service.JournalPostingService;

@RestController
@RequestMapping("/api/v1/journal-entries")
public class JournalEntryController {

    private final JournalPostingService journalPostingService;

    public JournalEntryController(JournalPostingService journalPostingService) {
        this.journalPostingService = journalPostingService;
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
}
