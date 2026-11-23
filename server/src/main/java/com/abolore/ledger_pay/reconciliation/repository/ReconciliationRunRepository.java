package com.abolore.ledger_pay.reconciliation.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.abolore.ledger_pay.reconciliation.entity.ReconciliationRun;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationRunStatus;

public interface ReconciliationRunRepository extends JpaRepository<ReconciliationRun, UUID> {

    List<ReconciliationRun> findByProviderAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(String provider,
            Instant periodStart, Instant periodEnd);

    Page<ReconciliationRun> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
            SELECT COUNT(r)
            FROM ReconciliationRun r
            WHERE r.provider = :provider
            AND r.status IN :statuses
            AND r.periodStart < :periodEnd
            AND r.periodEnd > :periodStart
            """)
    long countByProviderAndStatusInAndPeriodOverlap(String provider, Collection<ReconciliationRunStatus> statuses,
            Instant periodStart, Instant periodEnd);

}