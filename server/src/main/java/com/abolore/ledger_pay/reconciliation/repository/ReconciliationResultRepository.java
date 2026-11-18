package com.abolore.ledger_pay.reconciliation.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abolore.ledger_pay.reconciliation.entity.ReconciliationResult;

public interface ReconciliationResultRepository extends JpaRepository<ReconciliationResult, UUID> {

    List<ReconciliationResult> findByReconciliationRun_Id(UUID reconciliationRunId);
}