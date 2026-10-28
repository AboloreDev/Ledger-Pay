package com.abolore.ledger_pay.journal.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abolore.ledger_pay.journal.entity.JournalLines;

public interface JournalLineRepository extends JpaRepository<JournalLines, UUID> {

    List<JournalLines> findByJournalEntry_Id(UUID journalEntry_id);

    List<JournalLines> findByLedgerAccount_Id(UUID ledgerAccount_Id);
}
