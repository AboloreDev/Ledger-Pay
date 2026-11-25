package com.abolore.ledger_pay.journal.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.abolore.ledger_pay.journal.entity.JournalEntries;

public interface JournalEntryRepository extends JpaRepository<JournalEntries, UUID> {

    Optional<JournalEntries> findByReference(String reference);

    Optional<JournalEntries> findById(UUID journalId);

    boolean existsBySourceTypeAndSourceEventId(String sourceType, UUID sourceEventId);

    List<JournalEntries> findBySourceTypeAndSourceIdIn(String sourceType, Collection<UUID> sourceIds);

    @Query("""
            SELECT j FROM JournalEntries j
            JOIN Payment p ON j.sourceId = p.id
            WHERE j.sourceType = 'PAYMENT'
            AND p.merchant.id = :merchantId
            """)
    Page<JournalEntries> findAllByMerchantId(@Param("merchantId") UUID merchantId, Pageable pageable);

    @Query("""
            SELECT j FROM JournalEntries j
            JOIN Payment p ON j.sourceId = p.id
            WHERE j.id = :journalId
            AND j.sourceType = 'PAYMENT'
            AND p.merchant.id = :merchantId
            """)
    Optional<JournalEntries> findByIdAndMerchantId(
            @Param("journalId") UUID journalId,
            @Param("merchantId") UUID merchantId);

    @Query("""
            SELECT j FROM JournalEntries j
            JOIN Payment p ON j.sourceId = p.id
            WHERE j.reference = :journalReference
            AND j.sourceType = 'PAYMENT'
            AND p.merchant.id = :merchantId
            """)
    Optional<JournalEntries> findByReferenceAndMerchantId(
            @Param("journalReference") String journalId,
            @Param("merchantId") UUID merchantId);
}
