package com.abolore.ledger_pay.reconciliation.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reconciliation_runs")
public class ReconciliationRun {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 30)
    private String provider;

    @Column(name = "period_start", nullable = false)
    private Instant periodStart;

    @Column(name = "period_end", nullable = false)
    private Instant periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReconciliationRunStatus status;

    @Column(name = "total_provider_transactions", nullable = false)
    private int totalProviderTransactions;

    @Column(name = "total_internal_payments", nullable = false)
    private int totalInternalPayments;

    @Column(name = "matched_count", nullable = false)
    private int matchedCount;

    @Column(name = "discrepancy_count", nullable = false)
    private int discrepancyCount;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ReconciliationRun() {
    }

    public static ReconciliationRun create(String provider, Instant periodStart, Instant periodEnd) {

        if (periodStart == null || periodEnd == null || !periodStart.isBefore(periodEnd)) {
            throw new IllegalArgumentException(
                    "Invalid reconciliation period");
        }

        ReconciliationRun run = new ReconciliationRun();

        run.id = UUID.randomUUID();
        run.provider = provider;
        run.periodStart = periodStart;
        run.periodEnd = periodEnd;
        run.status = ReconciliationRunStatus.PENDING;
        run.totalProviderTransactions = 0;
        run.totalInternalPayments = 0;
        run.matchedCount = 0;
        run.discrepancyCount = 0;
        run.createdAt = Instant.now();

        return run;
    }

    public void start() {
        if (this.status != ReconciliationRunStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending reconciliation runs can start");
        }

        this.status = ReconciliationRunStatus.RUNNING;
        this.startedAt = Instant.now();
    }

    public void complete(int providerTransactions, int internalPayments, int matched, int discrepancies,
            boolean partial) {

        if (this.status != ReconciliationRunStatus.RUNNING) {
            throw new IllegalStateException(
                    "Only running reconciliation runs can complete");
        }

        this.totalProviderTransactions = providerTransactions;
        this.totalInternalPayments = internalPayments;
        this.matchedCount = matched;
        this.discrepancyCount = discrepancies;

        this.status = partial
                ? ReconciliationRunStatus.PARTIAL
                : ReconciliationRunStatus.COMPLETED;

        this.completedAt = Instant.now();
    }

    public void fail() {
        if (this.status != ReconciliationRunStatus.RUNNING) {
            throw new IllegalStateException(
                    "Only running reconciliation runs can fail");
        }

        this.status = ReconciliationRunStatus.FAILED;
        this.completedAt = Instant.now();
    }
}