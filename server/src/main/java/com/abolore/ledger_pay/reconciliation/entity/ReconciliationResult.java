package com.abolore.ledger_pay.reconciliation.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reconciliation_results")
public class ReconciliationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reconciliation_run_id", nullable = false)
    private ReconciliationRun reconciliationRun;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false, length = 30)
    private ReconciliationRecordType recordType;

    @Column(length = 100)
    private String reference;

    @Column(name = "internal_payment_id")
    private UUID internalPaymentId;

    @Column(nullable = false)
    private boolean matched;

    @Column(name = "discrepancies", columnDefinition = "jsonb")
    private String discrepancies;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ReconciliationResult() {
    }

    public static ReconciliationResult create(ReconciliationRun reconciliationRun, ReconciliationRecordType recordType,
            String reference, UUID internalPaymentId, boolean matched, String discrepancies) {

        ReconciliationResult result = new ReconciliationResult();

        result.reconciliationRun = reconciliationRun;
        result.recordType = recordType;
        result.reference = reference;
        result.internalPaymentId = internalPaymentId;
        result.matched = matched;
        result.discrepancies = discrepancies;
        result.createdAt = Instant.now();

        return result;
    }
}
