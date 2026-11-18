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

    @Column(name = "journals_checked", nullable = false)
    private int journalsChecked;

    @Column(name = "valid_journals", nullable = false)
    private int validJournals;

    @Column(name = "invalid_journals", nullable = false)
    private int invalidJournals;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 20)
    private ReconciliationTriggerType triggerType;

    @Column(name = "initiated_by")
    private String initiatedBy;

    protected ReconciliationRun() {
    }

    public static ReconciliationRun create(String provider, Instant periodStart, Instant periodEnd,
            ReconciliationTriggerType reconciliationTriggerType, String initiatedBy) {

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
        run.journalsChecked = 0;
        run.validJournals = 0;
        run.invalidJournals = 0;
        run.triggerType = reconciliationTriggerType;
        run.initiatedBy = initiatedBy;

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
            int journalsChecked, int validJournals, int invalidJournals, boolean partial) {

        if (this.status != ReconciliationRunStatus.RUNNING) {
            throw new IllegalStateException(
                    "Only running reconciliation runs can complete");
        }

        this.totalProviderTransactions = providerTransactions;
        this.totalInternalPayments = internalPayments;
        this.matchedCount = matched;
        this.discrepancyCount = discrepancies;

        this.journalsChecked = journalsChecked;
        this.validJournals = validJournals;
        this.invalidJournals = invalidJournals;

        this.status = partial
                ? ReconciliationRunStatus.PARTIAL
                : ReconciliationRunStatus.COMPLETED;

        this.completedAt = Instant.now();
    }

    public void fail(String reason) {
        if (this.status != ReconciliationRunStatus.RUNNING) {
            throw new IllegalStateException(
                    "Only running reconciliation runs can fail");
        }

        this.status = ReconciliationRunStatus.FAILED;
        this.failureReason = reason;
        this.completedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getProvider() {
        return provider;
    }

    public Instant getPeriodStart() {
        return periodStart;
    }

    public Instant getPeriodEnd() {
        return periodEnd;
    }

    public ReconciliationRunStatus getStatus() {
        return status;
    }

    public int getTotalProviderTransactions() {
        return totalProviderTransactions;
    }

    public int getTotalInternalPayments() {
        return totalInternalPayments;
    }

    public int getMatchedCount() {
        return matchedCount;
    }

    public int getDiscrepancyCount() {
        return discrepancyCount;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public int getJournalsChecked() {
        return journalsChecked;
    }

    public int getValidJournals() {
        return validJournals;
    }

    public int getInvalidJournals() {
        return invalidJournals;
    }

    public String getFailureReason() {
        return failureReason;
    }

}