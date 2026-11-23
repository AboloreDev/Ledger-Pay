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

    public static class Builder {
        private UUID id;
        private String provider;
        private Instant periodStart;
        private Instant periodEnd;
        private ReconciliationRunStatus status;
        private int totalProviderTransactions;
        private int totalInternalPayments;
        private int matchedCount;
        private int discrepancyCount;
        private Instant startedAt;
        private Instant completedAt;
        private Instant createdAt;
        private int journalsChecked;
        private int validJournals;
        private int invalidJournals;
        private String failureReason;
        private ReconciliationTriggerType triggerType;
        private String initiatedBy;

        public ReconciliationRun build() {
            return new ReconciliationRun(this);
        }

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setProvider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder setPeriodStart(Instant periodStart) {
            this.periodStart = periodStart;
            return this;
        }

        public Builder setPeriodEnd(Instant periodEnd) {
            this.periodEnd = periodEnd;
            return this;
        }

        public Builder setStatus(ReconciliationRunStatus status) {
            this.status = status;
            return this;
        }

        public Builder setTotalProviderTransactions(int totalProviderTransactions) {
            this.totalProviderTransactions = totalProviderTransactions;
            return this;
        }

        public Builder setTotalInternalPayments(int totalInternalPayments) {
            this.totalInternalPayments = totalInternalPayments;
            return this;
        }

        public Builder setMatchedCount(int matchedCount) {
            this.matchedCount = matchedCount;
            return this;
        }

        public Builder setDiscrepancyCount(int discrepancyCount) {
            this.discrepancyCount = discrepancyCount;
            return this;
        }

        public Builder setStartedAt(Instant startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder setCompletedAt(Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public Builder setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder setJournalsChecked(int journalsChecked) {
            this.journalsChecked = journalsChecked;
            return this;
        }

        public Builder setValidJournals(int validJournals) {
            this.validJournals = validJournals;
            return this;
        }

        public Builder setInvalidJournals(int invalidJournals) {
            this.invalidJournals = invalidJournals;
            return this;
        }

        public Builder setFailureReason(String failureReason) {
            this.failureReason = failureReason;
            return this;
        }

        public Builder setTriggerType(ReconciliationTriggerType triggerType) {
            this.triggerType = triggerType;
            return this;
        }

        public Builder setInitiatedBy(String initiatedBy) {
            this.initiatedBy = initiatedBy;
            return this;
        }
    }

    public ReconciliationRun() {
    }

    private ReconciliationRun(Builder builder) {
        this.id = builder.id;
        this.provider = builder.provider;
        this.periodStart = builder.periodStart;
        this.periodEnd = builder.periodEnd;
        this.status = builder.status;
        this.totalProviderTransactions = builder.totalProviderTransactions;
        this.totalInternalPayments = builder.totalInternalPayments;
        this.matchedCount = builder.matchedCount;
        this.discrepancyCount = builder.discrepancyCount;
        this.startedAt = builder.startedAt;
        this.completedAt = builder.completedAt;
        this.createdAt = builder.createdAt;
        this.journalsChecked = builder.journalsChecked;
        this.validJournals = builder.validJournals;
        this.invalidJournals = builder.invalidJournals;
        this.failureReason = builder.failureReason;
        this.triggerType = builder.triggerType;
        this.initiatedBy = builder.initiatedBy;
    }

    public static ReconciliationRun create(String provider, Instant periodStart,
            Instant periodEnd,
            ReconciliationTriggerType reconciliationTriggerType, String initiatedBy) {

        if (periodStart == null || periodEnd == null ||
                !periodStart.isBefore(periodEnd)) {
            throw new IllegalArgumentException(
                    "Invalid reconciliation period");
        }

        ReconciliationRun run = new ReconciliationRun.Builder()
                .setProvider(provider)
                .setPeriodStart(periodStart)
                .setPeriodEnd(periodEnd)
                .setStatus(ReconciliationRunStatus.PENDING)
                .setTotalProviderTransactions(0)
                .setTotalInternalPayments(0)
                .setMatchedCount(0)
                .setDiscrepancyCount(0)
                .setCreatedAt(Instant.now())
                .setJournalsChecked(0)
                .setValidJournals(0)
                .setInvalidJournals(0)
                .setTriggerType(reconciliationTriggerType)
                .setInitiatedBy(initiatedBy)
                .build();

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

    public ReconciliationTriggerType getTriggerType() {
        return triggerType;
    }

    public String getInitiatedBy() {
        return initiatedBy;
    }

}