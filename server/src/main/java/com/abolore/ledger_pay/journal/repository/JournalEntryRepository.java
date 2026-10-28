package com.abolore.ledger_pay.journal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abolore.ledger_pay.journal.entity.JournalEntries;

public interface JournalEntryRepository extends JpaRepository<JournalEntries, UUID> {

    Optional<JournalEntries> findByReference(String reference);

    boolean existsBySourceTypeAndSourceEventId(String sourceType, UUID sourceEventId);

}
