package com.abolore.ledger_pay.reconciliation.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abolore.ledger_pay.reconciliation.entity.ReconciliationRun;

public interface ReconciliationRunRepository extends JpaRepository<ReconciliationRun, UUID> {

    List<ReconciliationRun> findByProviderAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(String provider,
            Instant periodStart, Instant periodEnd);

}